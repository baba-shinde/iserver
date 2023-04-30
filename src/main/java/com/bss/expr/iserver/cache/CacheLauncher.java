package com.bss.expr.iserver.cache;

import com.bss.expr.iserver.pojo.Tick;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class CacheLauncher {
    private static final String TICK_CACHE = "TickCache";
    private IgniteCache<String, Tick> tickCache;
    private static final int TOTAL_ORDERS = 2000000;
    private Random random = new Random();
    private Gson gson;

    private List<String> instrumentList;

    @EventListener(ApplicationReadyEvent.class)
    public void startCache() throws Exception {
        gson = new Gson();
        Ignite ignite = Ignition.start("sql-config.xml");
        log.info("Cache is being created !");

        GsonJsonParser gsonJsonParser = new GsonJsonParser();

        CacheConfiguration<String, Tick> ordCacheCfg = new CacheConfiguration<>(TICK_CACHE);
        //ordCacheCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        //ordCacheCfg.setCacheMode(CacheMode.REPLICATED);
        ordCacheCfg.setIndexedTypes(String.class, Tick.class, Double.class, Long.class);
        ordCacheCfg.setCopyOnRead(false);
        ordCacheCfg.setStoreKeepBinary(true);
        /*
        ordCacheCfg.setSqlOnheapCacheEnabled(true);
        ordCacheCfg.setOnheapCacheEnabled(true);
        ordCacheCfg.setSqlOnheapCacheMaxSize(3000000);
        ordCacheCfg.setSqlOnheapCacheMaxSize(3000000);*/

        // Auto-close cache at the end of the example.

        tickCache = ignite.getOrCreateCache(ordCacheCfg);

        Thread.sleep(10000);

        loadOrderData();

        log.info("Total number of records loaded : {}", tickCache.size(CachePeekMode.ALL));

        for (int i=0; i<10; i++) {
            readBasedOnSide("select top 500 * from Tick where side = \'SELL\' order by ricCode");
        }

        for (int i=0; i<10; i++) {
            readBasedOnSide("select top 500 * from Tick order by ricCode");
        }

        for (int i=0; i<10; i++) {
            readBasedOnSide("select distinct ricCode from Tick");
        }

        for (int i=0; i<10; i++) {
           readBasedOnSide("select top 500 orderId,ricCode from Tick group by ricCode,orderId");
        }

        for (int i=0; i<10; i++) {
            readBasedOnSide("select top 500 * from Tick where side = \'SELL\'  and ricCode=\'AAVAS\'");
        }
    }

    public void readBasedOnSide(String sql) {
        //String sql = "select * from Tick where side = \'SELL\'";

        long start  = System.currentTimeMillis();
        List<List<?>> res = tickCache.query(new SqlFieldsQuery(sql).setDistributedJoins(true)).getAll();
        long finish = System.currentTimeMillis();
        System.out.println("Returned Result size : " + res.size() +" in " + (finish - start) + " for Query: " + sql);

        /*List<Tick> ticks = new ArrayList<>(1024);
        for (Object o: res) {
            Tick t = (Tick)o;
            ticks.add(t);
        }

        String jsonString = gson.toJson(ticks);


        System.out.println(jsonString);
         */
    }

    private int getRandomNumber(int number) {
        return random.ints(0, number)
                .findFirst()
                .getAsInt();
    }

    //Order-XX-
    private void loadOrderData() {
        long startTime = System.currentTimeMillis();
        for (int j=0; j<TOTAL_ORDERS; j++) {
            final int i = j;
            //dataLoaderExecutor.execute(()->{
                String id = "-XX-" + i;
            int randomNumber = getRandomNumber(instrumentList.size() - 1);
            Tick order = Tick.builder()
                        .orderId("Order" + id)
                        .ricCode(instrumentList.get(randomNumber))
                        .side(j%5 == 0 ? "SELL" : "BUY")
                        .date(new Date())
                        .price(1323.3*i)
                        .micCode("XNSE")
                        .quantity(10 * randomNumber)
                        .build();

                this.tickCache.put(order.getOrderId(), order);
            //});
        }
        System.out.println("Order Cache is fully populated ! finished in " + (System.currentTimeMillis() - startTime));
        log.info("Order Cache is fully populated !");
    }

    @Value("${iserver.instrument.list}")
    public void setInstrumentList(String list) {
        this.instrumentList = Arrays.asList(list.split(","));
    }
}
