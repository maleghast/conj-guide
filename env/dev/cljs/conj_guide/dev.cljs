(ns ^:figwheel-no-load conj-guide.dev
  (:require
    [conj-guide.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
