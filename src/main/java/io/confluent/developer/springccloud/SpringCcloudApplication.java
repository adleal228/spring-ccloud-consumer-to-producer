package io.confluent.developer.springccloud;

import adrian_db.belen_ad.ad.Key;
import adrian_db.belen_ad.ad.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.latlonschema;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import io.confluent.address;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;

@SpringBootApplication
public class SpringCcloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCcloudApplication.class, args);
	}

	@Bean
	NewTopic geolatlon() {
		return TopicBuilder.name("geo-latlon-topic").partitions(1).replicas(3).build();
	}

//	private static final String URL = "http://maps.googleapis.com/maps/api/geocode/json";
//	public GoogleResponse convertToLatLong(String fullAddress) throws IOException {
//
//		URL url = new URL(URL + "?address="
//				+ URLEncoder.encode(fullAddress, "UTF-8") + "&sensor=false");
//		// Open the Connection
//		URLConnection conn = url.openConnection();
//
//		InputStream in = conn.getInputStream();
//		ObjectMapper mapper = new ObjectMapper();
//		GoogleResponse response = (GoogleResponse) mapper.readValue(in, GoogleResponse.class);
//		in.close();
//		return response;
//
//	}
}


@Component
@RequiredArgsConstructor
class Consumer {
	private final KafkaTemplate<Integer, latlonschema> template;
	/*Consumer(KafkaTemplate<Integer, latlonschema> template) {
		this.template = template;
	}*/
	Random rand = new Random();
	int upperbound = 1000000;

	@KafkaListener(topics = {"adrian-db.belen_ad.ad"}, groupId = "geo-enrich-consumer-6", properties = { "auto.offset.reset=earliest", "specific.avro.reader=true"})
			//,topicPartitions = {@TopicPartition( topic = "adrian-db.belen_ad.ad", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "10000"))})
	public void consume(ConsumerRecord<Key, Value> record) throws IOException {
		System.out.println("received = " + record.value() + " with key " + record.key() + " at offset " + record.offset());
		Value value = record.value();
		System.out.println(value.getMapAddress().toString());
		float float_random_lat = rand.nextLong();
		String convertToLat = String.valueOf(float_random_lat);//value.getAddress().toString() + ", LAT:41234.124312";
		float float_random_lon = rand.nextLong();
		String convertToLon = String.valueOf(float_random_lon);
		//Producing to new topic after consuming and transforming
		template.setCloseTimeout(Duration.ofHours(1));

		latlonschema llvalue = new latlonschema(value.getTitle(), convertToLat, convertToLon);
		template.send("geo-latlon-topic", 1, llvalue);

		//GoogleResponse res = new SpringCcloudApplication().convertToLatLong(value.getAddress().toString());
//		if(res.getStatus().equals("OK"))
//		{
//			for(Result result : res.getResults())
//			{
//				System.out.println("Lattitude of address is :" + result.getGeometry().getLocation().getLat());
//				System.out.println("Longitude of address is :" + result.getGeometry().getLocation().getLng());
//				//System.out.println("Location is " + result.getGeometry().getLocation_type());
//			}
//		}
//		else
//		{
//			System.out.println(res.getStatus());
//		}
	}
}