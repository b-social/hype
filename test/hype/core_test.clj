(ns hype.core-test
  (:require
    [clojure.test :refer :all]

    [ring.mock.request :as ring]

    [camel-snake-kebab.core :as csk]

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
                             :thing-id   456}})))))

  (testing "expands path template parameter"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "/examples/{exampleId}"
            (hype/absolute-path-for routes :example
              {:path-template-params {:example-id :example-id}})))))

  (testing "expands path template parameters"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "/examples/{exampleId}/subexamples/{subExampleId}"
            (hype/absolute-path-for routes :sub-example
              {:path-template-params {:example-id     :example-id
                                      :sub-example-id :sub-example-id}})))))

  (testing "uses provided query parameter key function when supplied"
    (let [routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "/examples/{example_id}/subexamples/{sub_example_id}"
            (hype/absolute-path-for routes :sub-example
              {:path-template-params       {:example-id     :example-id
                                            :sub-example-id :sub-example-id}
               :path-template-param-key-fn csk/->snake_case_string})))))

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

  (testing "camel cases query parameters by default"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?perPage=10&sortDirection=descending"
            (hype/absolute-path-for routes :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}})))))

  (testing "uses provided query parameter key function when supplied"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples?per_page=10&sort_direction=descending"
            (hype/absolute-path-for routes :example
              {:query-params       {:per-page       10
                                    :sort-direction "descending"}
               :query-param-key-fn csk/->snake_case_string})))))

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
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]})))))

  (testing "camel cases query template parameters by default"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?perPage,sortDirection}"
            (hype/absolute-path-for routes :example
              {:query-template-params [:per-page :sort-direction]})))))

  (testing "uses provided query template parameter key function when supplied"
    (let [routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "/examples{?per_page,sort_direction}"
            (hype/absolute-path-for routes :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string}))))))

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
                             :thing-id   456}})))))

  (testing "expands path template parameter"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id] :example]]]]
      (is (= "https://example.com/examples/{exampleId}"
            (hype/absolute-url-for request routes :example
              {:path-template-params {:example-id :example-id}})))))

  (testing "expands path template parameters"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "https://example.com/examples/{exampleId}/subexamples/{subExampleId}"
            (hype/absolute-url-for request routes :sub-example
              {:path-template-params {:example-id     :example-id
                                      :sub-example-id :sub-example-id}})))))

  (testing "uses provided query parameter key function when supplied"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   [["/examples/" :example-id]
                    [["" :example]
                     [["/subexamples/" :sub-example-id] :sub-example]]]]]]
      (is (= "https://example.com/examples/{example_id}/subexamples/{sub_example_id}"
            (hype/absolute-url-for request routes :sub-example
              {:path-template-params       {:example-id     :example-id
                                            :sub-example-id :sub-example-id}
               :path-template-param-key-fn csk/->snake_case_string})))))

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

  (testing "camel cases query parameters by default"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?perPage=10&sortDirection=descending"
            (hype/absolute-url-for request routes :example
              {:query-params {:per-page       10
                              :sort-direction "descending"}})))))

  (testing "uses provided query parameter key function when supplied"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples?per_page=10&sort_direction=descending"
            (hype/absolute-url-for request routes :example
              {:query-params       {:per-page       10
                                    :sort-direction "descending"}
               :query-param-key-fn csk/->snake_case_string})))))

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
              {:query-params          {:key0 "value0"}
               :query-template-params [:key1 :key2]})))))

  (testing "camel cases query template parameters by default"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?perPage,sortDirection}"
            (hype/absolute-url-for request routes :example
              {:query-template-params [:per-page :sort-direction]})))))

  (testing "uses provided query template parameter key function when supplied"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          routes [""
                  [["/" :root]
                   ["/examples" :example]]]]
      (is (= "https://example.com/examples{?per_page,sort_direction}"
            (hype/absolute-url-for request routes :example
              {:query-template-params       [:per-page :sort-direction]
               :query-template-param-key-fn csk/->snake_case_string}))))))

(deftest absolute-path->absolute-url
  (testing "converts an absolute path to an absolute URL based on the request"
    (let [request (ring/request "GET" "https://example.com/some/thing")
          absolute-path "/examples/123"]
      (is (= "https://example.com/examples/123"
            (hype/absolute-path->absolute-url request absolute-path))))))
