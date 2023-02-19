clean:
    @rm -rf written

build:
    @cargo build -p text --bin import

import:
    @cargo run -p text --bin import