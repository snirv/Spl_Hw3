package bgu.spl181.net.api.bidi;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//I am snir
public class Movie extends  BaseMovie{

    protected long price;
    protected List<String> bannedCountries;
    protected  long availableAmount;//TODO need to write the int into String to json
    protected  long totalAmount;//TODO need to write the int into String to json
    protected transient AtomicBoolean lock;

    public Movie(long id, String name, Integer price, List<String> bannedCountries, int totalAmount) {
        super(id ,name);
        this.price = price;
        this.availableAmount = totalAmount;
        this.totalAmount =totalAmount;
        this.bannedCountries = bannedCountries;
        this.lock = new AtomicBoolean(false);
    }

    @Override
    public String toString() {
        if(bannedCountries != null && !bannedCountries.isEmpty()) {
            String bannedCountries = "";
            for (String country : this.bannedCountries) {
                bannedCountries = bannedCountries + "\""+ country +"\"" + " ";
            }
            bannedCountries = bannedCountries.substring(0, bannedCountries.length() - 1);
            return  "\"" +name +"\""+ " "  +availableAmount  + " " + price   +" "+ bannedCountries;
        }
        else {return    "\"" +name +"\""+ " "  +availableAmount  + " " + price ;}

        }


    public long getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String> getBannedCountries() {
        return bannedCountries;
    }

    public void setBannedCountries(List<String> bannedCountries) {
        this.bannedCountries = bannedCountries;
    }

    public long getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(long availableAmount) {
        this.availableAmount = availableAmount;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }
}
