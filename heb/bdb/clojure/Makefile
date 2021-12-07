all: fmt xml

run: 
	@clj -X:run :file ../BrownDriverBriggs.json
.PHONY: run

xml:
	@clj -X:xml :file ../BrownDriverBriggs.xml
.PHONY: run

cmd ?= fix
fmt:
	@clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.8.0"}}}' \
  -m cljfmt.main $(cmd)
.PHONY: fmt
