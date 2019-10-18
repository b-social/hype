(ns hype.core
  "Hypermedia functions for `bidi` and `ring`.

  `hype` currently provides support for:

    - generating paths or URLs,
    - including template parameters, and
    - converting between paths and URLs."
  (:require
    [clojure.string :as string]

    [bidi.bidi :refer [path-for]]
    [ring.util.codec :as codec]
    [camel-snake-kebab.core :as csk]))

(defn- query-string-for
  [parameters {:keys [key-fn]
               :or   {key-fn csk/->camelCaseString}}]
  (if (seq parameters)
    (str "?"
      (codec/form-encode
        (reduce-kv
          (fn [acc k v] (assoc acc (key-fn k) v))
          {}
          parameters)))
    ""))

(defn- query-template-for
  [parameter-names {:keys [expansion-character key-fn]
                    :or   {expansion-character "?"
                           key-fn              csk/->camelCaseString}}]
  (if (seq parameter-names)
    (str "{" expansion-character
      (string/join "," (map key-fn parameter-names))
      "}")
    ""))

(defn- path-template-params-for
  [parameter-definitions {:keys [key-fn]
                          :or   {key-fn csk/->camelCaseString}}]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (str "{" (key-fn v) "}")))
    {}
    parameter-definitions))

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
      - `path-template-params`: parameters that should remain templatable in
        the resulting path, specified as a map from path parameter name to
        template variable name, as keywords.
      - `path-template-param-key-fn`: a function to apply to path template
        variable names before including in the path, camel casing by default.
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-param-key-fn`: a function to apply to query parameter keys before
        including in the path, camel casing by default,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names,
      - `query-template-param-key-fn`: a function to apply to query template
        parameter keys before including in the path, camel casing by default.

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

      (absolute-path-for routes :article
        {:path-template-params {:id :article-id}})
      ; => \"/articles/{articleId}/article.html\"

      (absolute-path-for routes :article
        {:path-template-params {:id :articleID}
         :path-template-param-key-fn clojure.core/identity})
      ; => \"/articles/{articleID}/article.html\"

      (absolute-path-for routes :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-template-params [:per-page :page]})
      ; => \"/articles/index.html?latest=true&sortDirection=descending{&perPage,page}\"

      (absolute-path-for routes :article
        {:path-params {:id 10}
         :query-template-params [:include-author, :include-images]})
      ; => \"/articles/10/article.html{?includeAuthor,includeImages}\"

      (absolute-path-for routes :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-param-key-fn clojure.core/identity
         :query-template-params [:per-page :page]
         :query-template-param-key-fn clojure.core/identity})
      ; => \"/articles/index.html?latest=true&sort-direction=descending{&per-page,page}\""
  ([routes handler] (absolute-path-for routes handler {}))
  ([routes handler
    {:keys [path-params
            path-template-params
            path-template-param-key-fn
            query-params
            query-param-key-fn
            query-template-params
            query-template-param-key-fn]
     :or   {path-params                 {}
            path-template-params        {}
            path-template-param-key-fn  csk/->camelCaseString
            query-params                {}
            query-param-key-fn          csk/->camelCaseString
            query-template-params       []
            query-template-param-key-fn csk/->camelCaseString}
     :as   params}]
    (str
      (apply path-for routes handler
        (mapcat seq
          (merge
            (path-template-params-for
              path-template-params
              {:key-fn path-template-param-key-fn})
            path-params)))
      (query-string-for
        query-params
        {:key-fn query-param-key-fn})
      (query-template-for
        query-template-params
        {:expansion-character (if (empty? query-params) "?" "&")
         :key-fn              query-template-param-key-fn}))))

(defn absolute-url-for
  "Builds an absolute URL for `handler` based on `request`, `routes` and
  `params` where:

    - `handler` is a keyword identifying the handler for which to build a path,
    - `routes` is a bidi routes data structure,
    - `params` is an optional map which optionally includes any of:
      - `path-params`: parameters defined in the bidi routes as route patterns,
        specified as a map,
      - `path-template-params`: parameters that should remain templatable in
        the resulting path, specified as a map from path parameter name to
        template variable name, as keywords.
      - `path-template-param-key-fn`: a function to apply to path template
        variable names before including in the path, camel casing by default.
      - `query-params`: parameters that should be appended to the path as query
        string parameters, specified as a map,
      - `query-param-key-fn`: a function to apply to query parameter keys before
        including in the path, camel casing by default,
      - `query-template-params`: parameters that should be appended to the path
        as query string template parameters, specified as a sequence of
        parameter names,
      - `query-template-param-key-fn`: a function to apply to query parameter
        template keys before including in the path, camel casing by default.

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

      (absolute-url-for routes :article
        {:path-template-params {:id :article-id}})
      ; => \"https://localhost:8080/articles/{articleId}/article.html\"

      (absolute-url-for routes :article
        {:path-template-params {:id :articleID}
         :path-template-param-key-fn clojure.core/identity})
      ; => \"https://localhost:8080/articles/{articleID}/article.html\"

      (absolute-url-for request routes :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-template-params [:per-page :page]})
      ; => \"https://localhost:8080/articles/index.html?latest=true&sortDirection=descending{&perPage,page}\"

      (absolute-url-for request routes :article
        {:path-params {:id 10}
         :query-template-params [:include-author :include-images]})
      ; => \"https://localhost:8080/articles/10/article.html{?includeAuthor,includeImages}\"

      (absolute-url-for request routes :article-index
        {:query-params {:latest true
                        :sort-direction \"descending\"}
         :query-param-key-fn clojure.core/identity
         :query-template-params [:per-page :page]
         :query-template-param-key-fn clojure.core/identity})
      ; => \"https://localhost:8080/articles/index.html?latest=true&sort-direction=descending{&per-page,page}\""
  ([request routes handler] (absolute-url-for request routes handler {}))
  ([request routes handler params]
    (absolute-path->absolute-url
      request (absolute-path-for routes handler params))))
