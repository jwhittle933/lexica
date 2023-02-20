use std::{fs::File, path::Path};

#[derive(Clone, Debug)]
pub struct Index {
    //
}

impl Index {
    pub fn new(path: String) -> Result<Self, std::io::Error> {
        let idx = format!("{}/{}", path, "index");

        let index = if Path::new(&idx).is_file() {
            File::open(idx)?
        } else {
            File::create(idx)?
        };

        Ok(Self {})
    }
}
