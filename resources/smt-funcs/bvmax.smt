(define-fun bvmax ((x (_ BitVec 32)) (y (_ BitVec 32))) (_ BitVec 32)
    (if (bvsge x y) x y))