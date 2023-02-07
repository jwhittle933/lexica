use serde::{Deserialize, Serialize};
use sqlx::{postgres::PgRow, FromRow, Row};

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Book {
    pub id: i64,
    pub name: String,
}

impl FromRow<'_, PgRow> for Book {
    fn from_row(row: &PgRow) -> Result<Self, sqlx::Error> {
        let id = row.try_get("id")?;
        let name = row.try_get("name")?;
        Ok(Self { id, name })
    }
}
