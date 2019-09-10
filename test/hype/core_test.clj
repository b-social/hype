(ns hype.core-test
  (:require
    [clojure.test :refer :all]

    [ring.mock.request :as ring]

    [hype.core :as hype]))

(deftest urls
  (testing "base-url"
    (testing "returns the domain name for a url"
      (is (= "https://example.com"
            (hype/base-url
              (ring/request "GET" "https://example.com/some/thing"))))

      (is (= "http://another.example.com"
            (hype/base-url
              (ring/request "GET" "http://another.example.com/some/thing"))))))

  (testing "absolute-url-for"
    (testing "returns the absolute url for a route"
      (let [request (ring/request "GET" "https://example.com/some/thing")
            routes [""
                    [["/" :root]
                     ["/examples" :examples]]]]
        (is (= "https://example.com/examples"
              (hype/absolute-url-for request routes :examples)))))

    (testing "expands arguments"
      (let [request (ring/request "GET" "https://example.com/some/thing")
            routes [""
                    [["/" :root]
                     [["/examples/" :example-id] :example]]]]
        (is (= "https://example.com/examples/123"
              (hype/absolute-url-for request routes :example
                :example-id 123))))))

  (testing "parameterised-url-for"
    (testing "describes a single parameter"
      (let [request (ring/request "GET" "https://example.com/some/thing")
            routes [""
                    [["/" :root]
                     ["/examples" :examples]]]]
        (is (= "https://example.com/examples{?first}"
              (hype/parameterised-url-for request routes :examples
                [:first])))))

    (testing "describes multiple parameters"
      (let [request (ring/request "GET" "https://example.com/some/thing")
            routes [""
                    [["/" :root]
                     ["/examples" :examples]]]]
        (is (= "https://example.com/examples{?first,second}"
              (hype/parameterised-url-for request routes :examples
                [:first :second])))))

    (testing "mixes positional and query parameters"
      (let [request (ring/request "GET" "https://example.com/some/thing")
            routes [""
                    [["/" :root]
                     [["/examples/" :example-id] :example]]]]
        (is (= "https://example.com/examples/123{?first,second}"
              (hype/parameterised-url-for request routes :example
                [:first :second]
                :example-id 123)))))))
