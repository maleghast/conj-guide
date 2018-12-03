(ns conj-guide.prod
  (:require [conj-guide.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
