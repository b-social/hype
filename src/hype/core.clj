(ns hype.core
  "Hypermedia functions for `bidi` and `ring`.

  `hype` currently provides support for:

    - generating paths or URLs,
    - including template parameters, and
    - converting between paths and URLs."
  (:require
    [clojure.string :as string]

    [bidi.bidi :refer [path-for]]
    [ring.util.codec :as codec]))

(defn- query-string-for [parameters]
  (if (seq parameters)
    (str "?" (codec/form-encode parameters))
    ""))

(defn- query-template-for
  ([parameter-names] (query-template-for "?" parameter-names))
  ([expansion-character parameter-names]
    (if (seq parameter-names)
      (str "{" expansion-character
        (string/join "," (map name parameter-names))
        "}")
      "")))

(defn base-url-for
  "Returns the URL used to reach the server based on `request`.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string."
  [request]
  (let [scheme (-> request :scheme name)
        host (get-in request [:headers "host"])]
    (format "%s://%s" scheme host)))

(defn absolute-path->absolute-url
  "Builds an absolute URL by appending `absolute-path` to the base URL of
  `request`.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string."
  [request absolute-path]
  (str (base-url-for request) absolute-path))

(defn absolute-path-for
  "Builds an absolute path for `handler` based on `routes` and `params` where:

    - `handler` is a keyword identifying the handler for which to build a path,
    - `routes` is a bidi routes data structure,
    - `params` is an optional map which optionally includes any of:
      - `path-params`: parameters defined in the bidi routes as route patterns,
        specified as a map,
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names.

  The path is returned as a string.

  Examples:

      (def routes [\"/\" {\"index.html\" :index
                          \"articles/\" {\"index.html\" :article-index
                                         [:id \"/article.html\"] :article}}])

      (absolute-path-for routes :index)
      ; => \"/index.html\"

      (absolute-path-for routes :article
        {:path-params {:id 10}})
      ; => \"/articles/10/article.html\"

      (absolute-path-for routes :article-index
        {:query-params {:latest true
                        :sort \"descending\"}
         :query-template-params [:perPage :page]})
      ; => \"/articles/index.html?latest=true&sort=descending{&perPage,page}\"

      (absolute-path-for routes :article
        {:path-params {:id 10}
         :query-template-params [:includeAuthor, :includeImages]})
      ; => \"/articles/10/article.html{?includeAuthor,includeImages}\""
  ([routes handler] (absolute-path-for routes handler {}))
  ([routes handler
    {:keys [path-params query-params query-template-params]
     :or {query-params {}}
     :as params}]
    (str
      (apply path-for routes handler (mapcat seq path-params))
      (query-string-for query-params)
      (query-template-for
        (if (empty? query-params) "?" "&")
        query-template-params))))

(defn absolute-url-for
  "Builds an absolute URL for `handler` based on `request`, `routes` and
  `params` where:

    - `handler` is a keyword identifying the handler for which to build a path,
    - `routes` is a bidi routes data structure,
    - `params` is an optional map which optionally includes any of:
      - `path-params`: parameters defined in the bidi routes as route patterns,
        specified as a map,
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names.

  The `request` should be a ring request or equivalent. The URL is returned
  as a string.

  Examples:
      (require '[ring.mock.request :as ring-mock])

      (def request (ring-mock/request \"GET\" \"https://localhost:8080/help\"))
      (def routes [\"/\" {\"index.html\" :index
                          \"articles/\" {\"index.html\" :article-index
                                         [:id \"/article.html\"] :article}}])

      (absolute-url-for request routes :index)
      ; => \"https://localhost:8080/index.html\"

      (absolute-url-for request routes :article
        {:path-params {:id 10}})
      ; => \"https://localhost:8080/articles/10/article.html\"

      (absolute-url-for request routes :article-index
        {:query-params {:latest true
                        :sort \"descending\"}
         :query-template-params [:perPage :page]})
      ; => \"https://localhost:8080/articles/index.html?latest=true&sort=descending{&perPage,page}\"

      (absolute-url-for request routes :article
        {:path-params {:id 10}
         :query-template-params [:includeAuthor :includeImages]})
      ; => \"https://localhost:8080/articles/10/article.html{?includeAuthor,includeImages}\""
  ([request routes handler] (absolute-url-for request routes handler {}))
  ([request routes handler params]
    (absolute-path->absolute-url
      request (absolute-path-for routes handler params))))
