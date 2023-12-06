package khethelo.places.db.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import khethelo.places.model.Places;
import khethelo.places.model.Town;

/**
 * TODO: javadoc PlacesDb
 */
public class PlacesDb implements Places
{
    private Collection<String> validProvinces = new ArrayList<>();
    {
        validProvinces.add("Mpumalanga");
        validProvinces.add("KwaZulu-Natal");
        validProvinces.add("Gauteng");
        validProvinces.add("Limpopo");
        validProvinces.add("Free State");
        validProvinces.add("North West");
        validProvinces.add("Western Cape");
        validProvinces.add("Eastern Cape");
        validProvinces.add("Northern Cape");
    };
    private final Set<Town> towns = new TreeSet<>();
    private Collection<String> provinces;

    public PlacesDb( Set<Town> places ){
        towns.addAll( places );
        setProvinces();
    }

    public PlacesDb() {
    }

    private void setProvinces() {
        this.provinces = new ArrayList<>();
        for (Town town : towns) {
            if (!provinces.contains(town.getProvince()) && validProvinces.contains(town.getProvince())) {
                provinces.add(town.getProvince());
            }
        }
    }

    @Override
    public Collection<String> provinces(){
        return this.provinces;
    }

    @Override
    public Collection<Town> townsIn( String aProvince ){
        return towns.parallelStream()
            .filter( aTown -> aTown.getProvince().equals( aProvince ))
            .collect( Collectors.toSet() );
    }

    @Override
    public int size(){
        return towns.size();
    }

    @Override
    public void add(Town town) {
        this.towns.add(town);
        
    }

    @Override
    public Town get(int index) {
        try {
            return (Town) this.towns.toArray()[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    

}