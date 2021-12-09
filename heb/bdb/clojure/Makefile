all: fmt run

run:
	@clj -X:run :file ../BrownDriverBriggs.xml
.PHONY: run

cmd ?= fix
fmt:
	@clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.8.0"}}}' \
  -m cljfmt.main $(cmd)
.PHONY: fmt
