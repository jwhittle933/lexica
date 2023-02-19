use futures::{stream::FuturesUnordered, StreamExt};
use sqlx::postgres::PgPoolOptions;
use structopt::StructOpt;
use tracing::{error, info};

mod args;

use corelib::datastore::TextStore;
use text::extractor::{Extractor, Text};

use args::Args;

corelib::allocator!();

#[tokio::main]
async fn main() -> Result<(), std::io::Error> {
    let args = Args::from_args();
    info!(category = "initialize", "parsed args: {:?}", args);

    let pool = PgPoolOptions::new()
        .max_connections(150)
        .min_connections(1)
        .connect(&args.database_url)
        .await
        .unwrap_or_else(|e| {
            error!("Failed to connect to DB: {}", e);
            panic!("Failed to connect to DB: {}", e);
        });

    let extractor = Extractor::new(pool);

    let handles = FuturesUnordered::new();

    let mut books = extractor.books().await;
    while let Some(book) = books.next().await {
        let name = book.name.clone();
        let extr = extractor.clone();
        let out_dir = args.out.clone();
        info!("Book: {}", name);
        let lxx_store = TextStore::new_writer(&name, format!("{}/lxx", out_dir))?;
        let mt_store = TextStore::new_writer(&name, format!("{}/mt", out_dir))?;

        // new task for each book
        // use channel for synchonrizing
        handles.push(tokio::spawn(async move {
            let mut lxx = extr.lxx_by_book(book.clone()).await;
            let mut mt = extr.mt_by_book(book).await;

            loop {
                match (lxx.next().await, mt.next().await) {
                    (None, None) => break,
                    (Some(lxx_text), Some(mt_text)) => {
                        lxx_store.new_entry(
                            lxx_text.chapter,
                            lxx_text.verse,
                            lxx_text.subverse,
                            &lxx_text.text,
                        );

                        mt_store.new_entry(mt_text.chapter, mt_text.verse, None, &mt_text.text);
                    }
                    (Some(lxx_text), None) => {
                        lxx_store.new_entry(
                            lxx_text.chapter,
                            lxx_text.verse,
                            lxx_text.subverse,
                            &lxx_text.text,
                        );
                    }
                    (None, Some(mt_text)) => {
                        mt_store.new_entry(mt_text.chapter, mt_text.verse, None, &mt_text.text);
                    }
                }
            }
        }))
    }

    handles.for_each(|_| async {}).await;

    Ok(())
}
