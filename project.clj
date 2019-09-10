(defproject b-social/hype "0.0.16"
  :description "URL generation functions for bidi."
  :url "https://github.com/b-social/hype"

  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[bidi "2.1.6"]]

  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]
            [lein-eftest "0.5.8"]
            [lein-codox "0.10.7"]
            [lein-cljfmt "0.6.4"]
            [lein-kibit "0.1.6"]
            [lein-bikeshed "0.5.1"]]

  :profiles {:shared {:dependencies
                      [[org.clojure/clojure "1.10.0"]
                       [ring/ring-mock "0.4.0"]
                       [eftest "0.5.8"]]}
             :dev    [:shared {:source-paths ["dev"]
                               :eftest       {:multithread? false}}]
             :test   [:shared {:eftest {:multithread? false}}]}

  :cloverage
  {:ns-exclude-regex [#"^user"]}

  :codox
  {:namespaces  [#"^hype\."]
   :output-path "docs"
   :source-uri  "https://github.com/b-social/hype/blob/{version}/{filepath}#L{line}"}

  :cljfmt {:indents ^:replace {#".*" [[:inner 0]]}}

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org" :creds :gpg}}

  :release-tasks
  [["shell" "git" "diff" "--exit-code"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["codox"]
   ["changelog" "release"]
   ["shell" "sed" "-E" "-i" "" "s/\"[0-9]+\\.[0-9]+\\.[0-9]+\"/\"${:version}\"/g" "README.md"]
   ["shell" "git" "add" "."]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["vcs" "push"]]

  :aliases {"test"      ["with-profile" "test" "eftest" ":all"]
            "precommit" ["do"
                         ["check"]
                         ["kibit" "--replace"]
                         ["cljfmt" "fix"]
                         ["with-profile" "test" "bikeshed"
                          "--name-collisions" "false"
                          "--verbose" "true"]
                         ["test"]]})
