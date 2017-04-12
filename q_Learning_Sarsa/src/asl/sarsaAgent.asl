{include("learningAgent.asl")}
epsilon(0.3).
//!start. // Initial goal.

/* Plans */


+!keep_move : pos(I,J,R1,T) <-
	
   ?last_action(A);
   St1 = s(I,J,T);
   
   !select_next_action(St1,A1);
   !update(St1,A1,R1,A);
   ?last_state(S);
   M = S+1;
   -+last_state(M);
   !update_gui(M);
   -+state(M,pos(A,St1,R1));
   !continue_move(M,St1);
   !end_of_run(M).



+!update(St1,A1,R1,A) : qvalue(St1,_,_,_) <-
   !update1(St1,A1,R1,A).
+!update(St1,A1,R1,A) : not qvalue(St1,_,_,_) & non_terminal_state(St1) <-

   +qvalue(St1,up,0,0);
   +qvalue(St1,right,0,0);
   +qvalue(St1,down,0,0);
   +qvalue(St1,left,0,0);
   !update1(St1,A1,R1,A).
+!update(St1,A1,R1,A) : not qvalue(St1,_,_,_) & terminal_state(St1) <-
   +qvalue(St1,null,R1,0);
   !update1(St1,A1,R1,A).


+!update1(St1,A1,R1,A) : A \== null <-
   ?last_state(N);
   ?state(N,pos(_,St,R));
   ?qvalue(St,A,Q_St,N1);
   M1 = N1 + 1;
   !update_qvalue(St1,A1,R1,St,R,Q_St,A,M1).

+!update_qvalue(St1,A1,_,St,R,Q_St,A,M1) : non_terminal_state(St) <-
   ?gamma(Discount_Factor);
   Learning_Rate = 60.0/(M1+59.0);
   ?qvalue(St1,A1,Q_St1,N2);
   Q1 = Q_St + Learning_Rate *(R+Discount_Factor*Q_St1-Q_St);
   -qvalue(St,A,_,_);
   +qvalue(St,A,Q1,M1).
   
+!update_qvalue(_,_,_,St,R,_,null,M1) : terminal_state(St) <-
   -qvalue(St,null,_,_);
   +qvalue(St,null,R,M1).

+!update1(St1,A1,R1,null) : true <-
   true.


+!do_one_move(S) : true <-
   ?next_action(A);
   A;
   -+next_action(null);
   -+last_action(A).
   
+!select_next_action(S,A) <-
	.findall([Q,A1,N],qvalue(S,A1,Q,N),Qs);
   	?apply_f(Qs,Fs);

   	!epsilon_greedy_action_selection(Fs,S,A);
   	-+next_action(A).

//A1 = output parameter   
+!epsilon_greedy_action_selection(Fs,St,A1): Fs \== [] & epsilon(Eps) <-
	.random(Rand);
	!epsilon_greedy_action_selection(Fs,A1,Eps,Rand).

+!epsilon_greedy_action_selection(Fs,St,A1): Fs == [] <-
	!select_pure_random_action(St,A1).
	
+!epsilon_greedy_action_selection(Fs,A1,Eps,Rand):  Rand <= Eps <-
	!select_random_action(Fs,A1).
+!epsilon_greedy_action_selection(Fs,A1,Eps,Rand): Rand > Eps <-
	?best_action(Fs,A1).
+!select_random_action(Fs,A1) <- 
	.random(Rand);
	.length(Fs,Len);
	.nth(math.floor(Rand * Len),Fs,Pol);
	.nth(1,Pol,A1).
	
+!select_pure_random_action(St,A1) : non_terminal_state(St) & available_actions(As)<-
	.random(R);
	.length(As,Len);
	.nth(math.floor(R * Len),As,A1).
+!select_pure_random_action(St,A1) : terminal_state(St)<-
	A1 = null.

+epsilon(E)[source(percept)] <- .print("received epsilon");-+epsilon(E).

   
