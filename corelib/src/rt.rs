#[macro_export]
macro_rules! allocator {
    () => {
        use mimalloc::MiMalloc;

        #[global_allocator]
        static GLOBAL: MiMalloc = MiMalloc;
    };
}
