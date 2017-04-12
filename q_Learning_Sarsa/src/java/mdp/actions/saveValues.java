// Internal action code for project q_Learning_Sarsa

package mdp.actions;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class saveValues extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'mdp.actions.saveValues'");
        if (true) { // just to show how to throw another kind of exception
            throw new JasonException("not implemented!");
        }
        
        // everything ok, so returns true
        return true;
    }
}
