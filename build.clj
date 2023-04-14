(ns build
  (:require
   [borkdude.gh-release-artifact :as gh]
   [clojure.tools.build.api :as b])
  (:import
    [java.nio.file Paths]
    [com.google.cloud.tools.jib.api Jib Containerizer TarImage]
    [com.google.cloud.tools.jib.api.buildplan AbsoluteUnixPath]))

(def lib 'timokramer/datahike-distributed)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def current-commit (gh/current-commit))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-path (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "%s-%s-standalone.jar" (name lib) version))
(def uber-path (format "target/%s" uber-file))
(def image-name (format "docker.io/timokramer/%s:%s" (name lib) version))
(def latest-image-name (format "docker.io/timokramer/%s:latest" (name lib)))

(defn get-version
  [_]
  (println version))

(defn clean
  [_]
  (b/delete {:path "target"}))

(defn jar
  [_]
  (b/write-pom {:class-dir class-dir
                :src-pom "./template/pom.xml"
                :lib lib
                :version version
                :basis basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-path}))

(defn uber
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-path
           :basis basis
           :main (symbol (str (name lib) ".core"))}))

(defn image
  [_]
  (let [container-builder (-> (Jib/from "eclipse-temurin:17")
                              (.addLayer [(Paths/get uber-path (into-array String []))] (AbsoluteUnixPath/get "/"))
                              (.setProgramArguments (into-array String [ "java" "-jar" (format "/%s" uber-file)])))]
    (.containerize
     container-builder
     (Containerizer/to
      (-> (Paths/get "image.tar" (into-array String []))
          TarImage/at
          (.named image-name))))))

(comment
  (b/pom-path {:lib lib :class-dir class-dir})
  (clean nil)
  (jar nil)
  (uber nil)
  (image nil))
