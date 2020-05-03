(ns antq.dep.leiningen
  (:require
   [antq.record :as r]
   [clojure.java.io :as io]
   [clojure.walk :as walk]))

(def ^:private project-file "project.clj")

(defn extract-deps
  [project-clj-content-str]
  (let [deps (atom [])]
    (walk/postwalk (fn [form]
                     (when (and (sequential? form)
                                (= :dependencies (first form)))
                       (swap! deps concat (second form)))
                     form)
                   (read-string (str "(list " project-clj-content-str " )")))
    (for [[dep-name version] @deps]
      (r/map->Dependency {:type :java
                          :project project-file
                          :name  (str dep-name)
                          :version version}))))

(defn load-deps
  []
  (when (.exists (io/file project-file))
    (extract-deps (slurp project-file))))