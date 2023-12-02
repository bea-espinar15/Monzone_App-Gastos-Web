
package es.ucm.fdi.iw;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import es.ucm.fdi.iw.model.Debt;
import es.ucm.fdi.iw.model.DebtID;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.User;
import lombok.AllArgsConstructor;

/*
 *  -------------
 *    ALGORITHM
 *  -------------
 *  -> The algorithm uses a greedy technique to solve the problem.
 *  First, it iterates through the list of members to separate those with positive and negative balances 
 *  (zeros can be ignored as they don't need to pay anyone).
 *  
 *  Two priority queues are constructed, one for positive balances and another for negative balances. An element is 
 *  considered more prioritized than another if its absolute balance is larger (applicable to both queues).
 * 
 *  The problem is solved in a similar way to the knapsack problem when items can be split. Each positive balance 
 *  represents a knapsack, and the goal is to "fill" it (offset the balance) with the largest negative balance.
 * 
 *  If the negative balance (in absolute value) exceeds the positive balance, then the object is "split," and the 
 *  remaining value is re-inserted into the priority queue for negative balances. Otherwise, the updated positive 
 *  balance re-enters the positive queue.
 * 
 *  NOTE: This algorithm does not solve the problem optimally (in fact, it is an NP-complete problem), 
 *  but it improves efficiency compared to other algorithms.
 */

/*
 * Calculates debts given the members of a group
 */
public class DebtCalculator {

    // Aux class to represent elems in PriorityQueue
    @AllArgsConstructor
    public class Balance implements Comparable<Balance> {
        public User user;
        public float balance;

        @Override
        public int compareTo(Balance other) {
            return Float.compare((Math.abs(other.balance)), Math.abs(this.balance));
        }
    }

    public List<Debt> calculateDebts(List<Member> members, Group group) {

        PriorityQueue<Balance> positiveB = new PriorityQueue<>();
        PriorityQueue<Balance> negativeB = new PriorityQueue<>();

        // create priority queues
        for (Member m : members) {                 
            if (m.getBalance() < 0)
                negativeB.add(new Balance(m.getUser(), m.getBalance()));
            else if (m.getBalance() > 0)
                positiveB.add(new Balance(m.getUser(), m.getBalance()));
        }

        List<Debt> debts = new ArrayList<>();
        while (!positiveB.isEmpty()) {
            // get top of both queues
            Balance pos = positiveB.poll();
            Balance neg = negativeB.poll();

            // create transaction
            float amount = Math.min(Math.abs(pos.balance), Math.abs(neg.balance));
            User debtor = neg.user;
            User debtOwner = pos.user;
            DebtID dId = new DebtID(group.getId(), debtor.getId(), debtOwner.getId());
            debts.add(new Debt(dId, amount, group, debtor, debtOwner));
            // get spare balance and insert on queue
            float balance = pos.balance + neg.balance;
            
            if (Math.abs(balance) >= 0.01 && balance < 0)
                negativeB.add(new Balance(debtor, balance));
            else if (Math.abs(balance) >= 0.01 && balance > 0)
                positiveB.add(new Balance(debtOwner, balance));
        }
        return debts;
    }

}
