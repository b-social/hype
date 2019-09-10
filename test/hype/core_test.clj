(ns hype.core-test
  (:require
    [clojure.test :refer :all]

    [ring.mock.request :as ring]

    [hype.core :as hype]))

(deftest base-url-for
  (testing "returns the domain name for a URL"
    (is (= "https://example.com"
          (hype/base-url-for
            (ring/request "GET" "https://example.com/some/thing"))))

    (is (= "http://another.example.com"
          (hype/base-url-for
            (ring/request "GET" "http://another.example.com/some/thing"))))))

(deftest absolute-path-for
  (testing "returns the absolute path for a route"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :examples]]]]
      (is (= "/examples"
            (hype/absolute-path-for routes :examples)))))

  (testing "expands path parameter"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "/examples/123"
            (hype/absolute-path-for routes :example
              {:path-params {:example-id 123}})))))

  (testing "expands path parameters"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id "/thing/" :thing-id] :example]]]]
      (is (= "/examples/123/thing/456"
            (hype/absolute-path-for routes :example
              {:path-params {:example-id 123
                             :thing-id 456}})))))

  (testing "expands query parameter"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key=value"
            (hype/absolute-path-for routes :example
              {:query-params {:key "value"}})))))

  (testing "expands query parameters"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key1=value1&key2=value2"
            (hype/absolute-path-for routes :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}})))))

  (testing "expands query template parameter"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?key}"
            (hype/absolute-path-for routes :example
              {:query-template-params #{:key}})))))

  (testing "expands query template parameters"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?key1,key2}"
            (hype/absolute-path-for routes :example
              {:query-template-params [:key1 :key2]})))))

  (testing "appends query template parameters after query parameters"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?key0=value0{&key1,key2}"
            (hype/absolute-path-for routes :example
              {:query-params {:key0 "value0"}
               :query-template-params [:key1 :key2]}))))))

(deftest absolute-url-for
  (testing "returns the absolute URL for a route"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :examples]]]]
      (is (= "https://example.com/examples"
            (hype/absolute-url-for request routes :examples)))))

  (testing "expands path parameter"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "https://example.com/examples/123"
            (hype/absolute-url-for request routes :example
              {:path-params {:example-id 123}})))))

  (testing "expands path parameters"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id "/thing/" :thing-id] :example]]]]
      (is (= "https://example.com/examples/123/thing/456"
            (hype/absolute-url-for request routes :example
              {:path-params {:example-id 123
                             :thing-id 456}})))))

  (testing "expands query parameter"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key=value"
            (hype/absolute-url-for request routes :example
              {:query-params {:key "value"}})))))

  (testing "expands query parameters"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key1=value1&key2=value2"
            (hype/absolute-url-for request routes :example
              {:query-params {:key1 "value1"
                              :key2 "value2"}})))))

  (testing "expands query template parameter"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?key}"
            (hype/absolute-url-for request routes :example
              {:query-template-params #{:key}})))))

  (testing "expands query template parameters"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?key1,key2}"
            (hype/absolute-url-for request routes :example
              {:query-template-params [:key1 :key2]})))))

  (testing "appends query template parameters after query parameters"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?key0=value0{&key1,key2}"
            (hype/absolute-url-for request routes :example
              {:query-params {:key0 "value0"}
               :query-template-params [:key1 :key2]}))))))

(deftest absolute-path->absolute-url
  (testing "converts an absolute path to an absolute URL based on the request"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          absolute-path "/examples/123"]
      (is (= "https://example.com/examples/123"
            (hype/absolute-path->absolute-url request absolute-path))))))
