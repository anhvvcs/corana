(define-fun bvmin ((x (_ BitVec 32)) (y (_ BitVec 32))) (_ BitVec 32)
    (if (bvsge x y) y x))