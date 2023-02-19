pub struct JSONL;
pub struct JSON;
pub struct CSV;
pub struct KV;

/// The [FileWriter] writes files of a given format,
/// with the ability to create index files for quick
/// search within the data. The [FileWriter] is responsible
/// for managing only 1 file.
#[derive(Clone, Debug)]
pub struct FileWriter {
    _out_dir: String,
}

impl FileWriter {
    pub fn new(out_dir: String) -> Self {
        Self { _out_dir: out_dir }
    }
}
