(ns hello-world.core)

(def $field_w 640)
(def $field_h 480)
(def $scene)
(def $ctx)
(def $browser2game {
                    "z" :z,
                    "Z" :z,
                    "ArrowRight" :right,
                    "ArrowLeft" :left,
                    "ArrowUp" :up,
                    "ArrowDown" :down
                    })
(def $frame 0)
(def $game-state nil)

(defn random [n]
  (Math/floor (* n (Math/random))))

(defn make-entity [params]
  (let [entity (merge {:id nil, :x 0, :y 0,
                       :dx 1, :dy 1, :dir 0, :dir_time 0,
                       :update_frame 0, :state 0, :hp 0,
                       :move_type 0, :goal_x 0, :goal_y 0,
                       :explode_cnt 0, :big 0}
                      params)]
    (merge entity {:hit_box { :left 0, :right (entity :width), :top 0, :bottom (entity :height) },
                   :width (.-width (entity :id)),
                   :height (.-height (entity :id))}
           params)
  ))

;;   draw(ctx, frame) {
;;     ctx.drawImage(this.id, Math.round(this.x), Math.round(this.y));
;;     // ctx.fillStyle = "rgba(255,0,0,0.5)";
;;     var hit_box = this.hitBox();
;;     // ctx.fillRect(Math.round(this.x + hit_box.left) + 0.5,
;;     //              Math.round(this.y + hit_box.top) + 0.5,
;;     //              Math.round(hit_box.right - hit_box.left),
;;     //              Math.round(hit_box.bottom - hit_box.top));
;;     ctx.strokeStyle = "rgba(255,255,0,1)";
;;     ctx.lineWidth = 2;
;;     ctx.strokeRect(Math.round(this.x + hit_box.left),
;;                    Math.round(this.y + hit_box.top),
;;                    Math.round(hit_box.right - hit_box.left),
;;                    Math.round(hit_box.bottom - hit_box.top));
;;   }

(defn draw [obj]
  (.drawImage $ctx (obj :id) (Math/round (obj :x)) (Math/round (obj :y))))

;;   getCenter() {
;;     return [this.x + this.width / 2,
;;             this.y + this.height / 2];
;;   }

;;   moveTo(tx, ty) {
;;     if (this.x > tx) this.x--;
;;     else if (this.x < tx) this.x++;
;;     else if (this.y > ty) this.y--;
;;     else if (this.y < ty) this.y++;
;;   }
(defn move-to [obj tx ty]
  (cond
    (> (obj :x) tx) (update obj :x dec)
    (< (obj :x) tx) (update obj :x inc)
    (> (obj :y) ty) (update obj :y dec)
    (< (obj :y) ty) (update obj :y inc)))

;;   hitBox() {
;;     return this.hit_box;
;;   }

;;   collide(other) {
;;     if (this.state != 0 || other.state != 0)
;;       return;

;;     var a = this.hitBox(), b = other.hitBox();

;;     return (this.x + a.right > other.x + b.left &&
;;             this.x + a.left < other.x + b.right &&
;;             this.y + a.bottom > other.y + b.top &&
;;             this.y + a.top < other.y + b.bottom);
;;   }

;;   update(game_state) {}
;; }

;; class Character extends Entity {
;;   checkHp() {
;;     if (this.state == 0 && this.hp <= 0) {
;;       asset("explode").play();
;;       this.state = 2;
;;     }
;;   }
;; }

;; class GPin extends Character {
;;   draw(ctx, frame) {
;;     if (this.state == 2) {
;;       var [x, y] = this.getCenter();
;;       ctx.drawImage(asset("gpin_explode"), x - 32, y - 32);
;;     } else {
;;       Character.prototype.draw.apply(this, [ctx, frame]);
;;     }
;;   }

;;   hitBox() {
;;     if (this.big) {
;;       return { left: 16, right: 52, top: 12, bottom: 52 };
;;     } else {
;;       return { left: 8, right: 26, top: 6, bottom: 26 };
;;     }
;;   }

;;   takeKinoko() {
;;     if (!this.big) {
;;       this.id = asset("gpin64");
;;       this.width = this.height = 64;
;;       this.big = 1;
;;     }
;;     this.hp += 50;
;;   }

;;   update(game_state) {
;;     if (this.state == 0)
;;       moveChara(this, game_state);

;;     if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 100)
;;         this.state = 1;
;;     }
;;   }

;;   takeKusa(game_state) {
;;     this.hp += 10;
;;     game_state.addChara(new GPin({id: asset("gpin32"), x: this.x, y: this.y,
;;                                   hp: 40, dir: random(4),
;;                                   dir_time: 100 + random(80)}));
;;   }
;; }

;; class GPinPlayer extends GPin {
;;   takeKinoko() {
;;     if (!this.big) {
;;       this.id = asset("gpin64_p");
;;       this.width = this.height = 64;
;;       this.big = 1;
;;     }
;;     this.hp += 50;
;;   }

;;   update(game_state) {
;;     if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 100)
;;         this.state = 1;
;;     }
;;   }
;; }

;; class Mukku extends Character {
;;   draw(ctx, frame) {
;;     if (this.state == 2) {
;;       var [x, y] = this.getCenter();
;;       ctx.drawImage(asset("mukku_explode"), x - 32, y - 32);
;;     } else {
;;       Character.prototype.draw.apply(this, [ctx, frame]);
;;     }
;;   }

;;   hitBox() {
;;     if (this.big) {
;;       return { left: 16, right: 48, top: 12, bottom: 52 };
;;     } else {
;;       return { left: 8, right: 24, top: 6, bottom: 26 };
;;     }
;;   }

;;   takeKinoko() {
;;     if (this.big == 0) {
;;       this.id = asset("mukku64");
;;       this.width = this.height = 64;
;;       this.big = 1;
;;     }
;;     this.hp += 50;
;;     this.goal_x = this.goal_y = 0;
;;   }

;;   update(game_state) {
;;     if (this.state == 0) {
;;       if (this.move_type == 100)
;;         moveToKinokoKusa(this, game_state);
;;       else if (this.move_type == 2)
;;         moveType2(this, game_state);
;;       else if (this.move_type == 3)
;;         moveToPlayer(this, game_state);
;;       else
;;         moveChara(this, game_state);

;;       if (game_state.frame % (random(800) + 700) == 0)
;;         game_state.addChara(
;;           new Unk({id: asset("unk"),
;;                    x: this.x + 8, y: this.y + 8, state: 2,
;;                    hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));

;;       if (game_state.frame % (random(700) + 300) == 0) {
;;         var angle = Math.atan2(game_state.gpin_p.y - this.y, game_state.gpin_p.x - this.x);
;;         game_state.addChara(
;;           new Tama({id: asset("tama"),
;;                     x: this.x + 8, y: this.y + 8,
;;                     dx: 2*Math.cos(angle), dy: 2*Math.sin(angle),
;;                     hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));
;;       }
;;     } else if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 30)
;;         this.state = 1;
;;     }
;;   }

(defmethod entity-update :mukku [mukku gs]
  (cond
    (zero? (mukku :state))
    (let [mukku (case (mukku :move_type)
                   100 (move-to-kinoko-kusa mukku gs)
                   2 (move-type-2 mukku gs)
                   3 (move-to-player mukku gs)
                   :else (move-chara mukku))]

      (concat (if (zero? (mod $frame (+ (random 800) 700)))
                [{ :class :unk
                  :id (asset "unk")
                  :x (+ 8 (mukku :x))
                  :y (+ 8 (mukku :y))
                  :state 2
                  :hit_box { :left 3, :right 13, :top 3, :bottom 13}}]
                [])
              (if (zero? (mod $frame (+ (ramdom 700) 300)))
                (let [angle (Math/atan2 (- ((gs :gpin_p) :y) (mukku :y))
                                        (- ((gs :gpin_p) :x) (mukku :x)))]
                  [{ :class :tama
                    :x (+ 8 (mukku :x))
                    :y (+ 8 (mukku :y))
                    :dx (* 2 (Math/cos angle))
                    :dy (* 2 (Math/sin angle))
                    :hit_box { :left 3 :right 13 :top 3 :bottom 13 }}])
                [])
              [mukku]))

    (= 2 (mukku :state))
    (let [mukku (update mukku :explode_cnt inc)]
      [(if (= 30 (mukku :explode_cnt))
         (update mukku :state (constantly 1))
         mukku)])
    :else
    [mukku]))
             
;;   takeKusa(game_state) {
;;     this.hp += 10;
;;     this.goal_x = this.goal_y = 0;
;;     game_state.addChara(new Mukku({id: asset("mukku32"),
;;                                   x: this.x, y: this.y,
;;                                   hp: 40, move_type: random(10),
;;                                   dir: random(4), dir_time: 100 + random(80)}));
;;   }
;; }

;; class Tama extends Entity {
;;   update(game_state) {
;;     if (this.state == 0) {
;;       this.x += this.dx;
;;       this.y += this.dy;

;;       if (this.x + this.width < 0 ||
;;           this.y + this.height < 0||
;;           this.x > $field_w ||
;;           this.y > $field_h)
;;         this.state = 1;
;;     }
;;   }
;; }

;; class Unk extends Entity {
;;   update(game_state) {
;;     if (this.state == 2) {
;;       this.explode_cnt++;
;;       if (this.explode_cnt == 30)
;;         this.state = 0;
;;     }
;;   }
;; }

;; class Kinoko extends Entity {}
;; class Kusa extends Entity {}

;; class GameState {
;;   constructor(params = {}) {
;;     this.reset(params);
;;   }

(defn make-game-state []
  (reset {}))

;;   gcCharas() { this.list = this.list.filter(obj => obj.state != 1); }
(defn gc-charas [list]
  (remove #(= (% :state) 1) list))

;;   searchByClass(klass) { return this.all().filter(obj => obj instanceof klass); }
(defn search-by-class [gs class]
  (filter #(= class (% :class)) (all gs)))

;;   createGpinP() {
;;     return new GPinPlayer({id: asset("gpin32_p"),
;;                            x: random($field_w), y: random($field_h), hp: 50});
;;   }
(defn create-gpin-p []
  (make-entity {:class :gpin
                :id (asset "gpin32_p")
                :x (random $field_w)
                :y (random $field_h)
                :hp 50}))

;;   createFirstMukku() {
;;     return new Mukku({id: asset("mukku32"),
;;                       x: random($field_w), y: random($field_h), hp: 50,
;;                       move_type: 100,
;;                       dir: random(4), dir_time: 100 + random(80)});
;;   }
(defn create-first-mukku []
  (make-entity {:class :mukku,
                :id (asset "mukku32"),
                :x (random $field_w),
                :y (random $field_h),
                :hp 50,
                :move_type 100,
                :dir (random 4),
                :dir_time (+ 100 (random 80))}))

;;   reset(params = {}) {
;;     assignParameters(this, [["gpin_p", this.createGpinP()],
;;                             ["list",[]],
;;                             ["frame",0]],
;;                      params);
;;     this.addChara(this.createFirstMukku());
;;   }
(defn reset [gs]
  (let [gs1 (merge {:gpin_p (create-gpin-p) :list '() :frame 0} gs)]
    (add-chara gs1 (create-first-mukku))))

;;   addChara(chara) { this.list = [chara].concat(this.list); }
(defn add-chara [gs chara]
  (update gs :list #(cons chara %)))

;;   all() { return [[this.gpin_p], this.list].flat(); }
(defn all [gs]
  (cons (gs :gpin_p) (gs :list)))

;;   handleCollision() {
;;     var all = this.all();
;;     for (var i = 0; i < all.length - 1; i++) {
;;       for (var j = i + 1; j < all.length; j++) {
;;         var one = all[i], other = all[j];

;;         while (one.collide(other)) {
;;           if (one instanceof Character) {
;;             if (other instanceof Character) {
;;               this.charaCharaHit(one, other);
;;             } else if (other instanceof Kusa) {
;;               this.charaHitKusa(one, other);
;;             } else if (other instanceof Kinoko) {
;;               this.charaHitKinoko(one, other);
;;             } else if (other instanceof Unk) {
;;               this.charaUnkHit(one, other);
;;             } else if (other instanceof Tama) {
;;               this.charaTamaHit(one, other);
;;             }
;;           } else if (other instanceof Character) {
;;             [one, other] = [other, one]
;;             continue;
;;           }
;;           break;
;;         }
;;       }
;;     }
;;   }

;;   update(keystate) {
;;     if (this.defeat() || this.victory())
;;       $scene = "result";

;;     movePlayer(this.gpin_p, keystate);
;;     for (var obj of this.all())
;;       obj.update(this);
;;     if (this.frame % 120 == 0) {
;;       this.addChara(
;;         new Kusa({id: asset("kusa"),
;;                   x: random($field_w - 16), y: random($field_h - 16),
;;                   hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));
;;     }
;;     if (this.frame % 1720 == 0) {
;;       this.addChara(
;;         new Kinoko({id: asset("kinoko"),
;;                     x: random($field_w - 16), y: random($field_h - 16),
;;                     hit_box: { left: 3, right: 13, top: 3, bottom: 13}}))
;;     }
;;     this.handleCollision();
;;     for (var chara of this.searchByClass(Character))
;;       chara.checkHp();
;;     this.gcCharas();
;;     this.frame++;

;;     clearDisplay("black");
;;     for (var obj of this.frame%2==0 ? this.all() : this.all().reverse())
;;       obj.draw($ctx, this.frame);
;;     $ctx.drawImage(asset("gpin32_p"), 650, 190);
;;     drawStringSolid(`H P  ${this.gpin_p.hp}`, 690, 200,
;;                     { color: "white", font: "16px sans-serif" });
;;     $ctx.fillStyle = "white";
;;     $ctx.fillRect(640, 0, 1, 480);
;;   }

(defmulti entity-update (fn [obj gs] (obj :class)))

(defmethod entity-update :default [obj gs]
  obj)

(defn add-kinoko [list]
;;     if (this.frame % 1720 == 0) {
;;       this.addChara(
;;         new Kinoko({id: asset("kinoko"),
;;                     x: random($field_w - 16), y: random($field_h - 16),
;;                     hit_box: { left: 3, right: 13, top: 3, bottom: 13}}))
;;     }
  (if (zero? (mod $frame 1720))
    (cons { :class :kinoko
           :id (asset "kinoko")
           :x (random (- $field_w 16))
           :y (random (- $field_h 16))
           :hit_box { :left 3, :right 13, :top 3, :bottom 13 }} list)
    list))

(defn add-kusa [list]
;;     if (this.frame % 120 == 0) {
;;       this.addChara(
;;         new Kusa({id: asset("kusa"),
;;                   x: random($field_w - 16), y: random($field_h - 16),
;;                   hit_box: { left: 3, right: 13, top: 3, bottom: 13}}));
;;     }

  (if (zero? (mod $frame 120))
    (cons { :class :kusa
           :id (asset "kusa")
           :x (random (- $field_w 16))
           :y (random (- $field_h 16))
           :hit_box { :left 3, :right 13, :top 3, :bottom 13 }}
           list)
    list))

(defn game-state-update [gs]
  (clear-display "black")
  (doseq [obj (all gs)]
    (draw obj))
  (draw-status (gs :gpin_p))
  (draw-border)

  (let [gs1
        (merge gs
               {
                :gpin_p (first (entity-update (move-player (gs :gpin_p)) gs)),
                :list (gc-charas
                       (add-kinoko
                        (add-kusa
                         (mapcat #(entity-update % gs) (gs :list)))))
                })
        ]
    (set! $frame (+ 1 $frame))

    (let [next (if (or (defeat gs1) (victory gs1)) game-over-message game-state-update)]
      (list next gs1))))

(defn draw-status [gpin_p]
  (.drawImage $ctx (asset "gpin32_p") 650 190)
  (draw-string-solid (str "H P  " (gpin_p :hp)) 690 200
                     { :color "white" :font "16px sans-serif" }))

(defn draw-border []
  (set! (.-fillStyle $ctx) "white")
  (.fillRect $ctx 640 0 1 480))

;;   victory() {
;;     return !this.defeat() &&
;;       this.list.filter(obj => obj instanceof Mukku).length == 0;
;;   }

(defn victory [] false)

;;   defeat() {
;;     return this.gpin_p.state != 0;
;;   }

(defn defeat [] false)

;;   charaCharaHit(one, other) {
;;     if (one instanceof GPin && other instanceof GPin ||
;;         one instanceof Mukku && other instanceof Mukku)
;;       return;

;;     var tmp = one.hp;
;;     one.hp -= other.hp;
;;     other.hp -= tmp;
;;   }

;;   charaUnkHit(chara, unk) {
;;     if (chara instanceof Mukku) return;

;;     asset("hit_unk").play();
;;     unk.state = 1;
;;     chara.hp -= 5;
;;   }

;;   charaTamaHit(chara, tama) {
;;     if (chara instanceof Mukku) return;

;;     asset("hit_unk").play();
;;     tama.state = 1;
;;     chara.hp -= 10;
;;   }

;;   charaHitKusa(chara, kusa) {
;;     asset("hit_kusa").play();
;;     kusa.state = 1;
;;     chara.takeKusa(this)
;;   }

;;   charaHitKinoko(chara, kinoko) {
;;     chara.takeKinoko();
;;     kinoko.state = 1;
;;   }
;; }

;; function drawStringSolid(string, x, y, opts) {
;;   $ctx.font = opts.font;
;;   $ctx.fillStyle = opts.color;
;;   $ctx.textBaseline = "top";
;;   $ctx.fillText(string, x, y);
;; }

(defn draw-string-solid [string x y opts]
  (set! (.-font $ctx) (opts :font))
  (set! (.-fillStyle $ctx) (opts :color))
  (set! (.-textBaseline $ctx) "top")
  (.fillText $ctx string x y))

;; function gameOverMessage(game_state, keystate) {
;;   if (game_state.defeat()) {
;;     drawStringSolid("GAME OVER", 184, 200,
;;                     { color: "white", font: "80px sans-serif" });
;;   } else if (game_state.victory()) {
;;     clearDisplay("black");
;;     drawStringSolid("V I C T O R Y !!",
;;                     130, 64, { color: "white", font: "80px sans-serif" });
;;     drawStringSolid("C O N G R A T U L A T I O N S",
;;                     20, 196, { color: "white", font: "60px sans-serif" });
;;   }

;;   if (keystate.z) {
;;     $scene = "game";
;;     keystate.z = false;
;;     game_state.reset();
;;   }
;; }

;; function gameStartMessage(keystate) {
;;   clearDisplay("black");

;;   drawStringSolid("Gピン vs Mック", 120, 100,
;;                   { color: "white", font: "80px sans-serif" });
;;   drawStringSolid("PRESS Z TO START", 334, 308,
;;                   { color: "white", font: "16px sans-serif" });

;;   if (keystate.z) {
;;     $scene = "game";
;;     keystate.z = false;
;;   }
;; }

(defn game-start-message [keystate]
  (clear-display "black")
  (draw-string-solid "Gピン vs Mック" 120 100
                     { :color "white", :font "80px sans-serif" })
  (draw-string-solid "PRESS Z TO START" 334 308
                     { :color "white", :font "16px sans-serif" })

  (if (keystate :z)
    (set! $scene "game")
    (update keystate :z #(identity false))))


;; function asset(id) { return document.getElementById(id); }
(defn asset [id]
  (.getElementById js/document id))

;; function fixCharaPosition(chara) {
;;   if (chara.x < 0) {
;;     chara.x = 0;
;;     chara.dir = 1;
;;   }
;;   if (chara.y < 0) {
;;     chara.y = 0;
;;     chara.dir = 0;
;;   }
;;   if (chara.x > $field_w - chara.width) {
;;     chara.x = $field_w - chara.width;
;;     chara.dir = 2;
;;   }
;;   if (chara.y > $field_h - chara.height) {
;;     chara.y = $field_h - chara.height;
;;     chara.dir = 3;
;;   }
;; }

(defn fix-chara-position [chara]
  (reduce (fn [acc [field op limit new-dir]]
            (if (op (acc field) limit)
              (update (update acc field (constantly limit))
                      :dir (constantly new-dir))
              acc))
          chara
          [[:x < 0 1]
           [:y < 0 0]
           [:x > (- $field_w (chara :width)) 2]
           [:y > (- $field_h (chara :height)) 3]]))

(defn move-player [player]
  (if (zero? (player :state))
    (cond
      ($keystate :right) (update player :x + (player :dx))
      ($keystate :left) (update player :x - (player :dx))
      ($keystate :up) (update player :y - (player :dy))
      ($keystate :down) (update player :y + (player :dy))
      :else player)
    player))

;; function manhattan(x1, y1, x2, y2) {
;;   return Math.abs(x1 - x2) + Math.abs(y1 - y2);
;; }

(defn manhattan [x1 y1 x2 y2]
  (+ (Math/abs (- x1 x2)) (Math/abs (- y1 y2))))

;; function moveChara(chara, game_state) {
;;   if (chara.update_frame % chara.dir_time == 0) {
;;     chara.dir = random(4);
;;   }
;;   if (chara.dir == 0)
;;     chara.y += chara.dy;
;;   else if (chara.dir == 1)
;;     chara.x += chara.dx;
;;   else if (chara.dir == 2)
;;     chara.x -= chara.dx;
;;   else if (chara.dir == 3)
;;     chara.y -= chara.dy;
;;   fixCharaPosition(chara);
;;   chara.update_frame++;
;; }

(defn move-chara [chara gs]
  (let [chara
        (if (zero? (mod (chara :update_frame) (chara :dir_time)))
          (update chara :dir (constantly (random 4)))
          chara)]
    (let [chara
          (case (chara :dir)
            0 (update chara :y + (chara :dy))
            1 (update chara :x + (chara :dx))
            2 (update chara :x - (chara :dx))
            3 (update chara :y - (chara :dy)))]
      (let [chara (fix-chara-position chara)]
        (update chara :update_frame + 1)))))

;; function minBy(list, score_fn) {
;;   var ms = 1/0, me = null;
;;   for (var e of list) {
;;     var s = score_fn(e);
;;     if (s < ms) {
;;       ms = s;
;;       me = e;
;;     }
;;   }
;;   return me;
;; }

;; function moveToKinokoKusa(chara, game_state) {
;;   var dist = k => manhattan(chara.x, chara.y, k.x, k.y);
;;   var target = minBy(game_state.searchByClass(Kinoko), dist) ||
;;       minBy(game_state.searchByClass(Kusa), dist) || { x: 0, y: 0 };

;;   if (chara.big == 1) {
;;     chara.moveTo(target.x - 22, target.y);
;;   } else {
;;     chara.moveTo(target.x, target.y);
;;   }
;; }

(defn move-to-kinoko-kusa [chara gs]
  (letfn [(dist [k] (manhattan (chara :x) (chara :y) (k :x) (k :y)))]
    (let [target (or (min-key dist (filter #(= (% :class) :kinoko) (gs :list)))
                     (min-key dist (filter #(= (% :class) :kusa) (gs :list)))
                     { :x 0 :y 0 })]
      (if (= 1 (chara :big))
        (move-to chara (- (target :x) 22) (target :y))
        (move-to chara (target :x) (target :y))))))

;; function moveType2(chara, game_state) {
;;   if (chara.hp <= 20) {
;;     var kusa = game_state.searchByClass(Kusa)[0];
;;     if (kusa) {
;;       if (chara.goal_x == 0        &&  chara.goal_y == 0 ||
;;           chara.goal_x == chara.x  &&  chara.goal_y == chara.y) {
;;         chara.goal_x = kusa.x;
;;         chara.goal_y = kusa.y;
;;       }
;;       chara.moveTo(chara.goal_x, chara.goal_y);
;;     } else {
;;       moveChara(chara, game_state);
;;     }
;;   } else {
;;     var gpin1 = game_state.all().filter(e => e instanceof GPin &&
;;                                         !(e instanceof GPinPlayer))[0];
;;     if (gpin1)
;;       chara.moveTo(gpin1.x, gpin1.y);
;;     else
;;       moveChara(chara, game_state);
;;   }
;; }

(defn move-type-2 [chara gs]
  (move-chara chara gs))

;; function moveToPlayer(mukku, game_state) {
;;   mukku.moveTo(game_state.gpin_p.x, game_state.gpin_p.y);
;;   fixCharaPosition(mukku);
;; }

(defn move-to-player [mukku gs]
  (let [gpin_p (gs :gpin_p)]
    (fix-chara-position (move-to mukku (gpin_p :x) (gpin_p :y)))))

(defn clear-display [color]
  (set! (.-fillStyle $ctx) color)
  (.fillRect $ctx 0 0 840 480))

(def $keystate {:right false, :left false, :up false, :down false, :z false})

(defn set-key [ev, v]
  (when ($browser2game (.-key ev))
        (.preventDefault ev)
        (set! $keystate (merge $keystate {($browser2game (.-key ev)) v}))))

(defn gpin []
  (set! $ctx (.getContext (.getElementById js/document "screen") "2d"))
  (set! $frame 0)
  (set! $scene game-state-update)

  (set! $game-state (make-game-state))

  (set! (.-onkeydown js/document) (fn [ev] (set-key ev true)))
  (set! (.-onkeyup js/document) (fn [ev] (set-key ev false)))

  (letfn [(render [_timestamp]
            (let [[next gs1] ($scene $game-state)]
              (set! $scene next)
              (set! $game-state gs1))
            (.requestAnimationFrame js/window render))]
      (.requestAnimationFrame js/window render)))

(gpin)
