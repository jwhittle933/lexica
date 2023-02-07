pub mod book;
pub mod text;

use futures::TryStreamExt;
use futures_core::Stream;
use futures_util::{pin_mut, StreamExt};
use sqlx::{postgres::PgPool, FromRow};

pub use book::Book;
pub use text::Text;

use crate::extractor::text::LXX;

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
        let (tx, rx) = flume::unbounded();
        let mut conn = self.pool.acquire().await.expect("failed to get connection");

        tokio::spawn(async move {
            let mut rows = sqlx::query(r#" SELECT id, name FROM books"#).fetch(&mut conn);

            while let Some(row) = rows.try_next().await.expect("Could not query books") {
                tx.send_async(Book::from_row(&row).expect("failed to query book"))
                    .await
                    .expect("book tx send failed");
            }
        });

        rx.into_stream()
    }

    #[tracing::instrument(skip_all)]
    pub async fn texts_by_book<S>(&self, books: S) -> impl Stream<Item = Text<LXX>>
    where
        S: Stream<Item = Book>,
    {
        pin_mut!(books);
        let (tx, rx) = flume::unbounded();

        while let Some(book) = books.next().await {
            let mut conn = self.pool.acquire().await.expect("failed to get connection");
            let tx_clone = tx.clone();

            tokio::spawn(async move {
                let mut lxx_rows =
                    sqlx::query(r#"SELECT chapter, verse, text FROM lxx WHERE book_id = ?"#)
                        .bind(book.id)
                        .fetch(&mut conn);

                while let Some(row) = lxx_rows.try_next().await.expect("could not query texts") {
                    tx_clone.send_async(Text::<LXX>::from_row(&row).expect(""));
                }
            });
        }

        rx.into_stream()
    }
}
