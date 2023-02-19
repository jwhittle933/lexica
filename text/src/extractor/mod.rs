pub mod book;
pub mod text;

use futures::TryStreamExt;
use futures_core::Stream;
use sqlx::{postgres::PgPool, FromRow};

pub use book::Book;
pub use text::{Text, LXX, MT};
use tracing::info;

#[derive(Debug, Clone)]
pub struct Extractor {
    pool: PgPool,
}

impl Extractor {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    #[tracing::instrument(skip_all)]
    pub async fn books(&self) -> impl Stream<Item = Book> {
        info!("Querying books");
        let (tx, rx) = flume::unbounded();
        let mut conn = self.pool.acquire().await.expect("failed to get connection");

        let sender = tx.clone();
        tokio::spawn(async move {
            let mut rows = sqlx::query(r#" SELECT id, name FROM books"#).fetch(&mut conn);

            while let Some(row) = rows.try_next().await.expect("Could not query books") {
                let book = Book::from_row(&row).expect("failed to query book");
                info!("Book: {:?}", book);
                sender.send(book).expect("send book failed");
            }
        });

        drop(tx);

        rx.into_stream()
    }

    #[tracing::instrument(skip(self))]
    pub async fn mt_by_book(&self, book: Book) -> impl Stream<Item = Text<MT>> {
        let (tx, rx) = flume::unbounded();
        let mut conn = self.pool.acquire().await.expect("failed to get connection");

        let sender = tx.clone();
        tokio::spawn(async move {
            let mut rows = sqlx::query!(
                "SELECT chapter, verse, text FROM mt WHERE book_id = $1 ORDER BY chapter, verse",
                book.id
            )
            .map(|r| Text::<MT>::new(book.name.clone(), r.chapter as u32, r.verse as u32, r.text))
            .fetch(&mut conn);

            while let Some(text) = rows.try_next().await.expect("could not query texts") {
                sender.send_async(text).await.expect("tx send text failure");
            }
        });

        drop(tx);
        rx.into_stream()
    }

    #[tracing::instrument(skip(self))]
    pub async fn lxx_by_book(&self, book: Book) -> impl Stream<Item = Text<LXX>> {
        let (tx, rx) = flume::unbounded();
        let mut conn = self.pool.acquire().await.expect("failed to get connection");

        tokio::spawn(async move {
            let mut rows = sqlx::query!(
                "SELECT chapter, verse, text, subverse FROM lxx WHERE book_id = $1 ORDER BY chapter, verse",
                book.id
            )
            .map(|r| {
                Text::<LXX>::new(
                    book.name.clone(),
                    r.chapter as u32,
                    r.verse as u32,
                    r.text,
                    r.subverse,
                )
            })
            .fetch(&mut conn);

            while let Some(text) = rows.try_next().await.expect("could not query texts") {
                tx.send_async(text).await.expect("tx send text failure");
            }
        });

        rx.into_stream()
    }
}
