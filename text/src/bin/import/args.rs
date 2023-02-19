use serde::{Deserialize, Serialize};
use structopt::StructOpt;

#[derive(Clone, Debug, Deserialize, Serialize, StructOpt)]
pub struct Args {
    #[structopt(
        long,
        default_value = "postgresql://localhost/texts",
        env = "DATABASE_URL"
    )]
    pub database_url: String,

    #[structopt(short, long, default_value = "json")]
    pub format: String,

    #[structopt(short, long, default_value = "written", env = "OUT_DIR")]
    pub out: String,
}
