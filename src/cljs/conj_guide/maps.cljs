(ns conj-guide.maps
  (:require
   [reagent.core :as r]
   [cljsjs.leaflet]
   [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                      oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]))

;;; Map Component supporting Functions

(def markers-layer-atom (r/atom nil))
(def borders-layer-atom (r/atom nil))

(defn re-leaflet-update-markers
  [mapatom markers]
  (let [mapinst @mapatom
        markerslayer (if (nil? @markers-layer-atom)
                       (js/L.layerGroup)
                       @markers-layer-atom)]
    (.clearLayers markerslayer)
    (doall
    (for [marker markers]
      (-> (js/L.marker
          (array
            (:lat marker)
            (:long marker))
          #js {:icon (case (:icon marker)
                   "dark" (js/L.icon (clj->js
                                      {:iconUrl "/icon/map_markers/dark_new.png"
                                       :iconSize [30 42]
                                       :iconAnchor [15 41]
                                       :popupAnchor [0 -31]}))
                   "red" (js/L.icon (clj->js
                                     {:iconUrl "/icon/map_markers/red_new.png"
                                      :iconSize [30 42]
                                      :iconAnchor [15 41]
                                      :popupAnchor [0 -31]}))
                   "orange" (js/L.icon (clj->js
                                        {:iconUrl "/icon/map_markers/orange_new.png"
                                         :iconSize [30 42]
                                         :iconAnchor [15 41]
                                         :popupAnchor [0 -31]}))
                   "green" (js/L.icon (clj->js
                                       {:iconUrl "/icon/map_markers/green_new.png"
                                        :iconSize [30 42]
                                        :iconAnchor [15 41]
                                        :popupAnchor [0 -31]}))
                   (js/L.icon (clj->js
                               {:iconUrl "/icon/map_markers/green_new.png"
                                :iconSize [30 42]
                                :iconAnchor [15 41]
                                :popupAnchor [0 -31]})))})
         (.addTo markerslayer)
         (.bindPopup (:name marker)))))
    (reset! markers-layer-atom markerslayer)
    (.addTo @markers-layer-atom mapinst)
    (reset! mapatom mapinst)))

(defn get-status-colour
  [status]
  (case status
    1 "#66B92E"
    0 "#66B92E"
    -1 "#DA932C"
    -2 "#D65B4A"
    "#657B93"))

(defn set-all-borders-to-one-colour
  [borderscoll]
  (reduce-kv
   (fn
     [m k v]
     (assoc
      m
      k
      (assoc
       v
       :color
       "#657B93")))
   {}
   borderscoll))

(defn update-border-colour
  [borderscoll border-id new-colour]
  (assoc-in
   borderscoll
   [border-id :color]
   new-colour))

(defn get-border-style
  [color]
  #js {:color color
       :opacity 1
       :fillColor color
       :fillOpacity 0.25})

(defn re-leaflet-add-borders
  [mapatom bordernames borders]
  (let [mapinst @mapatom
        borderslayer (if (nil? @borders-layer-atom)
                       (js/L.layerGroup)
                       @borders-layer-atom)]
    (.clearLayers borderslayer)
    (doall
     (for [bordername bordernames]
       (-> (ocall
            (oget js/window "L")
            "geoJSON"
            (clj->js (get-in borders [bordername :coords]))
            #js {:style (get-border-style (get-in borders [bordername :color]))})
           (.addTo borderslayer))))
    (reset! borders-layer-atom borderslayer)
    (.addTo @borders-layer-atom mapinst)
    (reset! mapatom mapinst)))

;;; Map Component

(defn re-leaflet
  [params]
    (let [dn (r/atom nil)
        mapatom (r/atom nil)
        mn (:mapname params)
        lt (:latitude params)
        lg (:longitude params)
        z (:zoom-level params)
        producer-markers (:markers params)]
    (r/create-class
     {:component-did-mount (fn [ref]
                             (reset! dn (r/dom-node ref))
                             (let [lmap (js/L.map @dn)
                                   mappositioned (-> lmap (.setView (array lt lg) z))]
                               (.addTo (js/L.tileLayer "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png") mappositioned)
                               (reset! mapatom mappositioned)))
      :component-did-update (fn [this]
                              (let [newparams (r/props this)
                                    lmap @mapatom
                                    mappositionednew (-> lmap
                                                         (.panTo
                                                          (array
                                                           (:latitude newparams)
                                                           (:longitude newparams))
                                                          (:zoom-level newparams)))]
                                (re-leaflet-update-markers mapatom (:markers params))
                                (re-leaflet-add-borders mapatom (keys (:borders params)) (:borders params))
                                (reset! mapatom mappositionednew)))
      :display-name (str "Leaflet Map - " mn)
      :reagent-render (fn [params]
                        (when @mapatom
                          (re-leaflet-update-markers mapatom (:markers params))
                          (re-leaflet-add-borders mapatom (keys (:borders params)) (:borders params)))
                        [:div {:style {:height (:height params)}}])})))
