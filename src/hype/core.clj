(ns hype.core
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

(defn base-url-for [request]
  (let [scheme (-> request :scheme name)
        host (get-in request [:headers "host"])]
    (format "%s://%s" scheme host)))

(defn absolute-path->absolute-url [request absolute-path]
  (str (base-url-for request) absolute-path))

(defn absolute-path-for
  ([routes handler] (absolute-path-for routes handler {}))
  ([routes handler
    {:keys [path-params query-params query-template-params]
     :or {query-params {}}}]
    (str
      (apply path-for routes handler (mapcat seq path-params))
      (query-string-for query-params)
      (query-template-for
        (if (empty? query-params) "?" "&")
        query-template-params))))

(defn absolute-url-for
  ([request routes handler] (absolute-url-for request routes handler {}))
  ([request routes handler params]
    (absolute-path->absolute-url
      request (absolute-path-for routes handler params))))
