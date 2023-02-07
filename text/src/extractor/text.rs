use std::marker::PhantomData;

use serde::{Deserialize, Serialize};
use sqlx::{postgres::PgRow, FromRow};

pub struct LXX;
pub struct MT;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Text<Src = MT> {
    pub book: Option<String>,
    pub chapter: u32,
    pub verse: u32,
    pub text: String,
    pub subverse: Option<String>, // only for LXX, 3 Kingdoms 12:24
    _marker: PhantomData<Src>,
}

impl FromRow<'_, PgRow> for Text<LXX> {
    fn from_row(row: &PgRow) -> Result<Self, sqlx::Error> {
        //
    }
}

impl FromRow<'_, PgRow> for Text<MT> {
    fn from_row(row: &PgRow) -> Result<Self, sqlx::Error> {
        //
    }
}
