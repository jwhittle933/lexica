use std::marker::PhantomData;

use serde::{Deserialize, Serialize};

pub struct LXX;
pub struct MT;

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Text<Src> {
    pub book: String,
    pub chapter: u32,
    pub verse: u32,
    pub text: String,
    pub subverse: Option<String>, // only for LXX, 3 Kingdoms 12:24
    pub _marker: PhantomData<Src>,
}

impl Text<MT> {
    pub fn new(book: String, chapter: u32, verse: u32, text: String) -> Self {
        Self {
            book,
            chapter,
            verse,
            text,
            subverse: None,
            _marker: PhantomData,
        }
    }
}

impl Text<LXX> {
    pub fn new(
        book: String,
        chapter: u32,
        verse: u32,
        text: String,
        subverse: Option<String>,
    ) -> Self {
        Self {
            book,
            chapter,
            verse,
            text,
            subverse,
            _marker: PhantomData,
        }
    }
}

impl<Src> std::fmt::Display for Text<Src> {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{} {}:{} - {}",
            self.book, self.chapter, self.verse, self.text
        )
    }
}

impl std::fmt::Display for LXX {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("lxx")
    }
}

impl std::fmt::Display for MT {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("mt")
    }
}
