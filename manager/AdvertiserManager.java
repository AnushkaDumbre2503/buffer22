package manager;

import model.Advertiser;
import repository.AdvertiserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdvertiserManager {
    private AdvertiserRepository advertiserRepository;
    
    public AdvertiserManager() {
        this.advertiserRepository = new AdvertiserRepository();
    }
    
    public Advertiser createAdvertiser(String name, double totalBudget, double remainingBudget) throws SQLException {
        Advertiser advertiser = new Advertiser(name, totalBudget, remainingBudget);
        return advertiserRepository.create(advertiser);
    }
    
    public Advertiser createAdvertiser(String name, double budget) throws SQLException {
        return createAdvertiser(name, budget, budget);
    }
    
    public Optional<Advertiser> getAdvertiserById(int id) throws SQLException {
        Optional<Advertiser> advertiserOpt = advertiserRepository.findById(id);
        if (advertiserOpt.isPresent()) {
            Advertiser advertiser = advertiserOpt.get();
            advertiserRepository.loadConflicts(advertiser);
            return Optional.of(advertiser);
        }
        return Optional.empty();
    }
    
    public Optional<Advertiser> getAdvertiserByName(String name) throws SQLException {
        Optional<Advertiser> advertiserOpt = advertiserRepository.findByName(name);
        if (advertiserOpt.isPresent()) {
            Advertiser advertiser = advertiserOpt.get();
            advertiserRepository.loadConflicts(advertiser);
            return Optional.of(advertiser);
        }
        return Optional.empty();
    }
    
    public List<Advertiser> getAllAdvertisers() throws SQLException {
        List<Advertiser> advertisers = advertiserRepository.findAll();
        for (Advertiser advertiser : advertisers) {
            advertiserRepository.loadConflicts(advertiser);
        }
        return advertisers;
    }
    
    public List<Advertiser> getActiveAdvertisers() throws SQLException {
        List<Advertiser> advertisers = advertiserRepository.findWithBudget();
        for (Advertiser advertiser : advertisers) {
            advertiserRepository.loadConflicts(advertiser);
        }
        return advertisers;
    }
    
    public Map<Integer, Advertiser> getActiveAdvertiserMap() throws SQLException {
        List<Advertiser> advertisers = getActiveAdvertisers();
        Map<Integer, Advertiser> advertiserMap = new java.util.HashMap<>();
        for (Advertiser advertiser : advertisers) {
            advertiserMap.put(advertiser.getId(), advertiser);
        }
        return advertiserMap;
    }
    
    public boolean updateAdvertiser(Advertiser advertiser) throws SQLException {
        return advertiserRepository.update(advertiser);
    }
    
    public boolean updateAdvertiserBudget(int advertiserId, double newBudget) throws SQLException {
        return advertiserRepository.updateBudget(advertiserId, newBudget);
    }
    
    public boolean deleteAdvertiser(int id) throws SQLException {
        return advertiserRepository.delete(id);
    }
    
    public void addConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        advertiserRepository.addConflict(advertiser1Id, advertiser2Id);
    }
    
    public void removeConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        advertiserRepository.removeConflict(advertiser1Id, advertiser2Id);
    }
    
    public boolean deductBudget(int advertiserId, double amount) throws SQLException {
        Optional<Advertiser> advertiserOpt = advertiserRepository.findById(advertiserId);
        if (advertiserOpt.isPresent()) {
            Advertiser advertiser = advertiserOpt.get();
            if (advertiser.hasBudget(amount)) {
                advertiser.deductBudget(amount);
                return advertiserRepository.update(advertiser);
            }
        }
        return false;
    }
    
    public double getTotalRevenue() throws SQLException {
        return advertiserRepository.getTotalRevenue();
    }
    
    public int getActiveAdvertisersCount() throws SQLException {
        return advertiserRepository.getActiveAdvertisersCount();
    }
    
    public void initializeConflicts() throws SQLException {
        // Add some default conflicts (Apple vs Samsung, Nike vs Adidas)
        addConflict(1, 2); // Apple vs Samsung
        addConflict(4, 5); // Nike vs Adidas
    }
    
    public void replenishAllBudgets() throws SQLException {
        List<Advertiser> advertisers = getAllAdvertisers();
        for (Advertiser advertiser : advertisers) {
            advertiser.setRemainingBudget(advertiser.getTotalBudget());
            advertiserRepository.update(advertiser);
        }
    }
}
