{include("learningAgent.asl")}
minimal_exploration(700).

//!start. // Initial goal.

/* Plans */

+!keep_move : pos(I,J,R1,T) <-
   
// Determina ultima actiune executata A care m-a dus in starea curenta
   ?last_action(A);
// Construieste un obiect St1 = s/3 ce reprezinta starea curenta
   St1 = s(I,J,T);
// Actualizeaza informatia: actiunea A a produs (St1,R1)
   !update(St1,R1,A);
// Se memoreaza ca ultima stare (St1,R1) s-a obtinut in urma executiei lui A 
// -- M: indexul sub care a fost memorata ultima stare atinsa
// -- pos(A,St1,R1): s-a ajuns in starea St1 cu recompensa R1 executand A
   ?last_state(S);
   M = S+1;
   -+last_state(M);  
   !update_gui(M);
   -+state(M,pos(A,St1,R1));   
   !continue_move(M,St1).


   
+!update(St1,R1,A) : qvalue(St1,_,_,_) <-
// Daca starea St1 a mai fost vizitata treci direct la actualizarea utilitatii   
   !update1(St1,R1,A).
+!update(St1,R1,A) : not qvalue(St1,_,_,_) & non_terminal_state(St1) <-
// Daca starea St1 nu a mai fost utilizata atunci o creaza
// Initializeaza: 
// -- numarul de actiuni executate in starea St1 cu 0
// -- q-valoarea (actiune, utilitate) cu 0. Daca nu s-a mai ajuns in St1 
//    pana acum nu se stie cat se poate castiga executand actiuni in St1
//   +qvalue(St1,null,R1,0);
   +qvalue(St1,up,R1,0);
   +qvalue(St1,right,R1,0);
   +qvalue(St1,down,R1,0);
   +qvalue(St1,left,R1,0);
// Se trece la actualizarea q-valorilor folosind Q-learning
   !update1(St1,R1,A).
+!update(St1,R1,A) : not qvalue(St1,_,_,_) & terminal_state(St1) <-
// Daca starea St1 nu a mai fost utilizata atunci o creaza
// Initializeaza: 
// -- numarul de actiuni executate in starea St1 cu 0
// -- q-valoarea (actiune, utilitate) cu 0. Daca nu s-a mai ajuns in St1 
//    pana acum nu se stie cat se poate castiga executand actiuni in St1
//   +qvalue(St1,null,R1,0);
   +qvalue(St1,null,R1,0);
// Se trece la actualizarea q-valorilor folosind Q-learning
   !update1(St1,R1,A).

// Daca nu s-a plecat din starea initiala
// A --> (St1,R1)
+!update1(St1,R1,A) : A \== null <-
// Determina indicele ultimei stari vizitate (din care s-a plecat)
   ?last_state(N);
// Determina ultima stare vizitata: St --A--> St1
   ?state(N,pos(_,St,R));
// Determina q-valoarea (St1,A)
   ?qvalue(St,A,Q_St,N1);
// Incrementeaza contorul de actiuni executate in ultima stare
   M1 = N1+1;
// Actualizeaza q-valoarea pentru starea St si actiunea A
   !update_qvalue(St1,R1,St,R,Q_St,A,M1).
+!update_qvalue(St1,_,St,R,Q_St,A,M1) : non_terminal_state(St) <-
// Determina q-valorile starii curente   
   .findall(Q,qvalue(St1,A1,Q,_),Qs);
   ?maxim_list(Qs,Q_St1);
   ?gamma(G);
   Q1 = Q_St + 0.9*(R+G*Q_St1-Q_St);

   -qvalue(St,A,_,_);
   +qvalue(St,A,Q1,M1).
+!update_qvalue(_,R1,St,_,_,null,M1) : terminal_state(St) <-
   -qvalue(St,null,_,_);
   +qvalue(St,null,R1,M1).
   
   // Daca s-a plecat din starea initiala nu este nimic de actualizat. 
// Incepe o noua "incercare" (trial)   
+!update1(St1,R1,null) : true <-
   true.

+!do_one_move(S) : true <-
   !det_policy(S,A);
   A;
   -+last_action(A).
   
+!det_policy(S,A) : true <-
   .findall([Q,A1,N],qvalue(S,A1,Q,N),Qs);
   ?apply_f(Qs,Fs);
	?best_action(Fs,A).
   
