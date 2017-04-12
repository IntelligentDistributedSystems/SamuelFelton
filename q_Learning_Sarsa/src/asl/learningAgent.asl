last_state(0).
last_trial(0).
last_action(null).
next_action(null).
limit(500000).
terminal_state(s(_,_,t)).
non_terminal_state(s(_,_,n)).
optimistic_reward(2).
minimal_exploration(0).
gamma(1.0).

check_limit(1000).

available_actions([left,right,up,down]).


exploration_function(Q,N,F) :-
   minimal_exploration(Nmin) &
   exploration_function(Q,N,Nmin,F).

exploration_function(_,N,Nmin,Q) :-
   N < Nmin &
   optimistic_reward(Q).
exploration_function(Q,N,Nmin,Q) :-
   N >= Nmin.
   
apply_f([[Q,A,N]|Qs],[[F,A]|Fs]) :-
   exploration_function(Q,N,F) &
   apply_f(Qs,Fs).
apply_f([],[]).
 
best_action([[F,A]|Fs],BestA) :-
   best_action(Fs,[F,A],BestA).
best_action([],[_,At],At).
best_action([[F,A]|Fs],[Ft,At],BestA) :-
   F > Ft &
   best_action(Fs,[F,A],BestA).
best_action([[F,A]|Fs],[Ft,At],BestA) :-
   F <= Ft &
   best_action(Fs,[Ft,At],BestA).
     
below_limit(N) :-
   limit(L) &
   N < L.

check_trial(N) :-
   check_limit(L) &
   (N mod L) == 0 &
   print_results &
   .print("trial: ",N).
check_trial(N) :-
   check_limit(L) &
   (N mod L) \== 0.
   
   
   
print_results :-
   .findall(q(St,A,Q,N),qvalue(St,A,Q,N),QL) &
   .sort(QL,QL1) &
   print_utilities(QL1).
   
print_utilities([]).
print_utilities([q(St,A,Q,N) | QL]) :-
   .print(St,"  ",A,"  ",Q,"  ",N) &
   print_utilities(QL).

maxim_list([Q | Qs],Q2) :-
   maxim_list(Qs,Q,Q2).
maxim_list([],Q,Q).
maxim_list([Q1 | Qs],Q,Q2) :-
   Q1 <= Q &
   maxim_list(Qs,Q,Q2).
maxim_list([Q1 | Qs],Q,Q2) :-
   Q1 > Q &
   maxim_list(Qs,Q1,Q2).


+!start : true <- 
	!clear_run_data;
   .print("I am an agent that keeps moving.");
   !keep_move.
   
-!keep_move : not pos(I,J,R1,T) <-
	request_update;!keep_move.

+!update_gui(M): M mod 1000 == 0 <-
	.findall(q(St,A,Q,N),qvalue(St,A,Q,N),QL);
   	send_q_values(QL).
+!update_gui(M) <-
	true.

+!continue_move(M,St) : non_terminal_state(St) & below_limit(M) <-
   !do_one_move(St);
   !!keep_move.

+!continue_move(M,St) : below_limit(M) & terminal_state(St)  <-
   ?last_trial(N);
   N1 = N+1;
   -+last_trial(N1);
   ?check_trial(N1);
   null;
   -+last_action(null);
   !keep_move.

+!continue_move(M,St) : not below_limit(M)   <-
   ?last_trial(N);
   .print("NO. OF TRIALS: ",N);
   .print("END OF RUN !");
   ?print_results.


+!clear_run_data <-
	.abolish(qvalue(_,_,_,_));
	-+last_state(0);
	-+last_trial(0);
	-+last_action(null).

+must_run <- !start.

+!end_of_run(M): limit(L) & M >= L <- -must_run[source(_)];!clear_run_data;.
+!end_of_run(M) <- true.



