use sqlx::postgres::PgPoolOptions;
use structopt::StructOpt;
use tracing::{error, info};

mod args;

use text::extractor::Extractor;
use text::task;

use args::Args;

#[tokio::main]
async fn main() {
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

    let books = extractor.books().await;
}
