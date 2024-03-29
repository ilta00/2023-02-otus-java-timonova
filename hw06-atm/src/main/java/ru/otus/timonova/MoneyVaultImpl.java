package ru.otus.timonova;

import ru.otus.timonova.exceptions.CannotGetSumException;
import ru.otus.timonova.exceptions.IllegalBanknoteNominalException;
import ru.otus.timonova.exceptions.NotEnoughMoneyException;

import java.util.*;

public class MoneyVaultImpl implements MoneyVault {

    private Map<Integer, Queue<Banknote>> banknoteMap = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            o1 = -o1;
            o2 = -o2;
            return o1.compareTo(o2);
        }
    });

    public Map<Integer, Queue<Banknote>> getBanknoteMap() {
        return banknoteMap;
    }

    public void initMoneyVault(Banknote... args) {
        Arrays.stream(args).toList().forEach(banknote -> {
            banknoteMap.put(banknote.getNominal(), new LinkedList<Banknote>());
        });
    }

    public void putBanknote(Banknote banknote) {
        int nominal = banknote.getNominal();
        if (!banknoteMap.containsKey(nominal)) {
            throw new IllegalBanknoteNominalException();
        }
        Queue<Banknote> queue = banknoteMap.get(nominal);
        queue.add(banknote);
    }

    public int getBalance() {
        int balance = 0;
        for (Map.Entry<Integer, Queue<Banknote>> entry : banknoteMap.entrySet()) {
            balance = balance + entry.getKey() * entry.getValue().size();
        }
        return balance;
    }

    public List<Banknote> getSum(int sum) throws NotEnoughMoneyException, CannotGetSumException {
        int balance = getBalance();
        if (balance < sum) {
            throw new NotEnoughMoneyException();
        }

        List<Banknote> banknoteList = new ArrayList<>();
        int curSum = sum;
        int prepareSum = 0;
        Map<Integer, List<Banknote>> banknoteListMap = new HashMap<>();
        Map<Integer, Queue<Banknote>> banknoteMapHistory = copyVault(banknoteMap);

        for (Map.Entry<Integer, Queue<Banknote>> entry : banknoteMap.entrySet()) {
            int nominal = entry.getKey();
            List<Banknote> banknoteCurList = getMaximumBanknotesForSum(curSum, nominal, entry.getValue());
            banknoteList.addAll(banknoteCurList);
            banknoteListMap.put(nominal, banknoteCurList);

            int entrySum = nominal * banknoteCurList.size();
            curSum = curSum - entrySum;
            prepareSum = prepareSum + entrySum;
        }

        if (curSum != 0) {
            banknoteMap = banknoteMapHistory;
            throw new CannotGetSumException();
        }

        return banknoteList;
    }

    private List<Banknote> getMaximumBanknotesForSum(int sum, int nominal, Queue<Banknote> queue) {
        List<Banknote> banknoteList = new ArrayList<>();
        int curSum = sum;
        while (curSum >= nominal && queue.size() > 0) {
            banknoteList.add(queue.poll());
            curSum = curSum - nominal;
        }
        return banknoteList;
    }

    private Map<Integer, Queue<Banknote>> copyVault(Map<Integer, Queue<Banknote>> map) {
        return Map.copyOf(map);
    }

}
