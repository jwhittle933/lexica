pub mod index;
pub mod kv;

use std::io::{BufWriter, Write};
use std::{fs::File, path::Path};

/// On-disk datastore
#[derive(Debug)]
pub struct TextStore {
    writer: File,
}

impl TextStore {
    /// Opens an existing store file or creates a new one.
    #[tracing::instrument]
    pub fn new_writer(book: &str, path: String) -> Result<Self, std::io::Error> {
        if !Path::new(&path).exists() {
            std::fs::create_dir_all(&path)?;
        }

        let idx = format!("{}/{}", path, "index");
        let file_location = format!("{}/{}.jsonl", path, book);

        let writer = if Path::new(&file_location).is_file() {
            let f = File::open(file_location)?;
            f.set_len(0)?;
            f
        } else {
            File::create(file_location)?
        };

        // let index = if Path::new(&idx).is_file() {
        //     File::open(idx)?
        // } else {
        //     File::create(idx)?
        // };

        Ok(Self { writer })
    }

    /// Adds a new text entry
    #[tracing::instrument(skip(self))]
    pub fn new_entry(
        &self,
        chapter: u32,
        verse: u32,
        partition: Option<String>,
        content: &str,
    ) -> Result<(), std::io::Error> {
        let value = serde_json::json!({
            "ref": format!("{}:{}{}", chapter, verse, partition.unwrap_or_default()),
            "text": content
        });

        let mut f = BufWriter::new(&self.writer);
        f.write_all(value.to_string().as_bytes())
            .and(f.write("\n".as_bytes()).map(|_| ()))
    }
}
