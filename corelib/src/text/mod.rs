use std::marker::PhantomData;

use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Text<Src> {
    pub book: String,
    pub chapter: u32,
    pub verse: u32,
    pub text: String,
    pub partition: Option<String>, // only for LXX, 3 Kingdoms 12:24
    pub _marker: PhantomData<Src>,
}
