last_state(0).
last_trial(0).
last_action(null).
limit(10000).
terminal_state(s(_,_,t)).
non_terminal_state(s(_,_,n)).
below_limit(N) :-
   limit(L) &
   N < L.
   

print_results :-
   .findall(u(St,U,N),utility(St,U,N),UL) &
   print_utilities(UL).

print_utilities([]).
print_utilities([u(St,U,N) | UL]) :-
   .print(St," ",U," ",N) &
   print_utilities(UL).

   
+!clear_run_data <-
	-utility(_,_,_);
	-+last_state(0);
	-+last_trial(0);
	-+last_action(null);.

//!start. // Initial goal.

/* Plans */

+!start : true <- 
   .print("I am an agent that keeps moving.");
   !clear_run_data;
   .findall(p(R,C,S),policy(s(R,C),S),L);
   
   send_policies(L);
   
   !keep_move;
   -policy(_,_)[source(percept)];
   !send_utilities.
   
   
+!send_utilities <-
	.findall(u(St,U,N),utility(St,U,N),UL);
   	send_utilities(UL).

+!keep_move : pos(I,J,R,T) <-

   ?last_action(A);
   St = s(I,J,T);
   !update(St,R,A);
   ?last_state(S);
   M = S+1;
   -+last_state(M);
   -+state(M,pos(A,St,R));  
   !continue_move(M,St).


+!update(St,R,A) : utility(St,_,_) <-
   !update1(St,R,A).
+!update(St,R,A) : not utility(St,_,_) <-
   +utility(St,R,0);
   !update1(St,R,A). 
+!update1(St,R,A) : A \== null <-
   ?last_state(N);
   ?state(N,pos(_,St1,R1));
   ?utility(St1,U1,N1);
   M1 = N1+1;
   ?utility(St,U,_);
   U2 = U1 + 60/(M1+59)*(R1+1.0*U-U1);
   -utility(St1,_,_);
   +utility(St1,U2,M1).
+!update1(St,R,null) : true <-
   true.
   

 
+!continue_move(M,St) : non_terminal_state(St) & below_limit(M) <-
   !do_one_move(St);
   !keep_move.
  

+!continue_move(M,St) : below_limit(M) & terminal_state(St)  <-
//   .print("END OF TRIAL !");
   -+last_action(null);
   ?last_trial(N);
   N1 = N+1;
   -+last_trial(N1);
   null;
   !keep_move.
   
+!continue_move(M,St) : not below_limit(M)   <-
   ?last_trial(N);
   .print("NO. OF TRIALS: ",N);
   .print("END OF RUN !");
   ?print_results.

+!do_one_move(s(I,J,n)) : true <-
   ?policy(s(I,J),A);
   -+last_action(A);
   A.
   
+policy(s(R,C),D)[source(percept)] <- 
	-policy(s(R,C),_)[source(percept),source(self)];
	+policy(s(R,C),D).
+must_run[source(percept)] <- !start;-must_run[source(percept)].
