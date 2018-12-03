(ns conj-guide.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [reitit.frontend :as reitit]
              [clerk.core :as clerk]
              [accountant.core :as accountant]
              [cljs.reader :refer [read-string]]
              [conj-guide.maps :refer [re-leaflet
                                       update-border-colour
                                       set-all-borders-to-one-colour
                                       get-status-colour]]))

(defonce app-state (atom {:borders {:dt {:coords {:type "FeatureCollection"
                                                  :features [{:type "Feature"
                                                  :properties {}
                                                  :geometry {:type "Polygon"
                                                             :coordinates [
                                                                  [[-78.90561103820801
                                                                    35.996974575069956]
                                                                    [-78.90536427497864
                                                                    35.99692249419066]
                                                                    [-78.90507459640502
                                                                    35.99655792707266]
                                                                    [-78.90125513076782
                                                                    35.994318406377765]
                                                                    [-78.89913082122803
                                                                    35.993320149920635]
                                                                    [-78.89883041381835
                                                                    35.99334619155392]
                                                                    [-78.89733910560606
                                                                    35.99488263269992]
                                                                    [-78.89727473258972
                                                                    35.99516040417593]
                                                                    [-78.89732837677002
                                                                    35.99537741246091]
                                                                    [-78.89755368232727
                                                                    35.99579406669441]
                                                                    [-78.897864818573
                                                                    35.99620203849867]
                                                                    [-78.89803647994995
                                                                    35.9968270124893]
                                                                    [-78.89819741249084
                                                                    35.99736518056842]
                                                                    [-78.89869093894958
                                                                    35.99783390461304]
                                                                    [-78.89931321144103
                                                                    35.99812902573051]
                                                                    [-78.9043664932251
                                                                    35.99817242580169]
                                                                    [-78.90510678291321
                                                                    35.997964105242175]
                                                                    [-78.90529990196228
                                                                    35.997625583159355]
                                                                    [-78.90540719032288
                                                                    35.99739990096356]
                                                                    [-78.90561103820801
                                                                     35.996974575069956]]]}}]}
                                         :color "#4A90E2"
                                         :centroid {:lat 35.99599
                                                    :long -78.90131}
                                         :zoom-level 17}}
                          :markers-ga [{:name "Durham Convention Centre"
                                        :lat 35.997425
                                        :long -78.902188
                                        :icon "red"}
                                       {:name "Durham Arts Society"
                                        :lat 35.997806
                                        :long -78.903558
                                        :icon "red"}]
                          :markers-pta [{:name "Lucky's Deli"
                                         :lat 35.996834
                                         :long -78.904467
                                         :icon "green"}
                                        {:name "Bull City Burgers and Brewery"
                                         :lat 35.995602
                                         :long -78.899779
                                         :icon "green"}
                                        {:name "Béyu Café"
                                         :lat 35.996699
                                         :long -78.903862
                                         :icon "green"}
                                        {:name "M Sushi"
                                         :lat 35.997252
                                         :long -78.901172
                                         :icon "green"}
                                        {:name "M Tempura"
                                         :lat 35.996159
                                         :long -78.900400 
                                         :icon "green"}]}))
;; 
;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/getting-around" :getting-around]
    ["/places-to-eat" :places-to-eat]]))

(def orders [{:status 1}])

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

(path-for :about)
;; -------------------------
;; Page components

(defn home-page []
  (fn []
    [:span.main
     [:h1 "Welcome to Conj-Guide 2018"]
     [:img {:src "/img/conj-guide-home.png"}]
     [:p "This is a general guide to finding your way around Downtown in Durham while you are at the Conj in 2018."]]))



(defn getting-around-page []
  (fn []
    [:span.main
     [:h1 "Getting Around"]
     [:p "Here is a Map of Downtown Durham"]
     ;; Map goes here
     [:div {:class "map-wrapper"}
      [re-leaflet
       {:mapname "getting-around"
        :latitude 35.99599
        :longitude -78.90131
        :zoom-level 17
        :height 650
        :markers (:markers-ga @app-state)
        :borders (:borders @app-state)}]]]))

(defn places-to-eat-page []
  (fn []
    [:span.main
     [:h1 "Places to Eat"]
     [:p "Downtown Durham has many great food options, here is another map with a few of them marked out for you."]
     ;; Another Map goes here
     [:div {:class "map-wrapper"}
      [re-leaflet
       {:mapname "places-to-eat"
        :latitude 35.99599
        :longitude -78.90131
        :zoom-level 17
        :height 650
        :markers (:markers-pta @app-state)
        :borders (:borders @app-state)}]]]))


;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :getting-around #'getting-around-page
    :places-to-eat #'places-to-eat-page))

;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :getting-around)} "Getting Around"] " | "
         [:a {:href (path-for :places-to-eat)} "Places to Eat"]]]
       [page]
       [:footer
        "©ScholaNoctis Ltd. 2018"]])))

;; -------------------------
;; Initialize app



(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
