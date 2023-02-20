use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct LXX(pub Text);
#[derive(Debug, Serialize, Deserialize)]
pub struct MT(pub Text);

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Text {
    pub book: String,
    pub chapter: u32,
    pub verse: u32,
    pub text: String,
    pub partition: Option<String>, // only for LXX, 3 Kingdoms 12:24
}

impl std::fmt::Display for Text {
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
        f.write_str("LXX ").and(f.write_str(&self.0.to_string()))
    }
}

impl std::fmt::Display for MT {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("MT ").and(f.write_str(&self.0.to_string()))
    }
}
