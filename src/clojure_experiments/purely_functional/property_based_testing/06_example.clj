(ns clojure-experiments.purely-functional.property-based-testing.06-example
  "Check https://purelyfunctional.tv/lesson/an-overview-with-an-example-test/
  Also Unicode FAQ: https://unicode.org/faq/casemap_charprop.html"
  (:require
   [clojure.string :as string]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.generators :as gen]))

;;;; Testing clojure.string/upper-case

(string/upper-case "abcd")

;;; as a demonstration
(comment

  (defspec always-passes 100
    (prop/for-all
     []
     true))

  (defspec always-passes 100
    (prop/for-all
     []
     false))

  ;; remove this dummy tests
  (remove-ns 'clojure-experiments.purely-functional.property-based-testing.06-example)

  ;; end
  )

;;; Process of writing propery-based tests
;;; 1. start with a property you need to test
;;; may seem hard to come up with

;; let's start with sanity checking - so to know that the fn doesn't do anything crazy
;; like throwing exceptions

;; if you like to see what the generator does you can "sample" it
;; beware that this output may kill the Clojure REPL (compiler unable to process weird characters??)
(gen/sample gen/string)

;; if you want more thatn 10 (default)
;; beware that this output may kill the Clojure REPL (compiler unable to process weird characters??)
(gen/sample gen/string 15)

(defspec length-doesnt-change
  ;; here we need to define a generateor => require clojure.test.check.generators
  (prop/for-all
   [s gen/string]
   (= (count s)
      (count (string/upper-case s)))))
;; Cider's error message isn't that helpful in this case
;; Fail in length-doesnt-change
;; expected: result
;; actual: false          
;; input: ["xy"]
;; 
;; => CHECK THE REPL!
;; especially the `:shrunk`
;; {:result false,
;;  :result-data {},
;;  :seed 1563873202651,
;;  :failing-size 66,
;;  :num-tests 67,
;;  :fail ["..."],
;;  :shrunk {:total-nodes-visited 21, :depth 6, :result false, :result-data {}, :smallest ["..."]},
;;  :test-var "length-doesnt-change"}

;; check that thing!
;; -> so the number of characters can actually change!!! 
;; is it actually bug in the implementation or 
;; DOES THIS KILL THE REPL?!?
#_(string/upper-case "...")
;; => "SS"



;; => we decided to improve our generator: use `gen/a`
(defspec length-doesnt-change
  ;; here we need to define a generateor => require clojure.test.check.generators
  (prop/for-all
   [s gen/string-ascii]
   (= (count s)
      (count (string/upper-case s)))))
;; => now it passes


;;; Let's write another test
(defspec everything-upercased
  (prop/for-all
   [s gen/string]
   (every? #(Character/isUpperCase %) (string/upper-case s))))
;; Fail in everything-upercased

;; expected: result

;; actual: false          
;; input: [" "]
;; DOES THIS KILL THE REPL?
#_(string/upper-case " ")

;; what's happening? is my test wrong?
;; what it does for symbols?
;; DOES THIS KILL THE REPL?
#_(Character/isUpperCase \))
;; => false

;; => fix the test
(defspec everything-upercased
  (prop/for-all
   [s gen/string]
   (every? #(if (Character/isLetter %)
              (Character/isUpperCase %)
              true)
           (string/upper-case s))))
;; => STILL FAILING:
;; {:result false,
;;  :result-data {},
;;  :seed 1563873958790,
;;  :failing-size 19,
;;  :num-tests 20,
;;  :fail ["..."],
;;  :shrunk {:total-nodes-visited 17, :depth 5, :result false, :result-data {}, :smallest ["..."]},
;;  :test-var "everything-upercased"}
#_(Character/isUpperCase ...)
;; => false

;; => TIME TO CHECK UNIQCODE FAQ!
;; https://unicode.org/faq/casemap_charprop.html
;; - some characters doesn't really have an uppercase variant: https://unicode.org/faq/casemap_charprop.html#7a

;; => we're going to use `string-ascii`
(defspec everything-upercased
  (prop/for-all
   [s gen/string-ascii]
   (every? #(if (Character/isLetter %)
              (Character/isUpperCase %)
              true)
           (string/upper-case s))))



;;; Let's write the final test - idempotence
;;; This works for non-ascii too!
(defspec idempotent
  (prop/for-all
   [s gen/string]
   (= (string/upper-case s)
      (string/upper-case (string/upper-case s)))))
