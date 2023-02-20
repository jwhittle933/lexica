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
        info!("Book: {}", name);

        let extr = extractor.clone();
        let out_dir = args.out.clone();

        let lxx_store = TextStore::new_writer(&name, format!("{}/lxx", out_dir))?;
        let mt_store = TextStore::new_writer(&name, format!("{}/mt", out_dir))?;

        // new thread for each book
        handles.push(tokio::spawn(async move {
            let mut lxx = extr.lxx_by_book(book.clone()).await;
            let mut mt = extr.mt_by_book(book).await;

            loop {
                match (lxx.next().await, mt.next().await) {
                    (None, None) => break,
                    (Some(lxx_text), Some(mt_text)) => {
                        add_text(&lxx_store, lxx_text).expect("lxx write failed");
                        add_text(&mt_store, mt_text).expect("mt write failed");
                    }
                    (Some(text), None) => {
                        add_text(&lxx_store, text).expect("lxx write failed");
                    }
                    (None, Some(text)) => {
                        add_text(&mt_store, text).expect("mt write failed");
                    }
                }
            }
        }))
    }

    Ok(handles.for_each(|_| async {}).await)
}

fn add_text<Src>(store: &TextStore, text: Text<Src>) -> Result<(), std::io::Error> {
    store.new_entry(text.chapter, text.verse, text.subverse, &text.text)
}
