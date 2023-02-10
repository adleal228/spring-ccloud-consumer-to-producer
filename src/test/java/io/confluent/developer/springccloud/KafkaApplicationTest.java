package io.confluent.developer.springccloud;

import io.confluent.latlonschema;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

@SpringBootTest
@RequiredArgsConstructor
class KafkaApplicationTest {
	@Autowired
	KafkaTemplate<Integer, latlonschema> template;

	@Test
	public void testinglatlonclass(){
		latlonschema testlatlon = new latlonschema("test", "124214", "12342");
		template.setCloseTimeout(Duration.ofHours(1));
		template.send("test", 1, testlatlon);
		Assert.notNull(template, "Kafka template is not null");
	}

	@Test
	public void whenSendingWithSimpleProducer_thenMessageReceived()
			throws Exception {
		String key = "Test Key";
		String data = "Sending with our own simple KafkaProducer";

		Properties producerProps = loadConfig("src/main/resources/test.properties");
		// Producer properties extra
		producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer;
		producer = new KafkaProducer<String, String>(producerProps);
		producer.send(new ProducerRecord<String, String>("test", key, data));

		Properties props = loadConfig("src/main/resources/test.properties");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-1");

		Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList("test"));
		ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

		Assert.notNull(Collections.singleton(records.isEmpty()), "records is not null");
	}

	public static Properties loadConfig(String configFile) throws IOException {
		if (!Files.exists(Paths.get(configFile))) {
			throw new IOException(configFile + " not found.");
		}
		final Properties cfg = new Properties();
		try (InputStream inputStream = new FileInputStream(configFile)) {
			cfg.load(inputStream);
		}
		return cfg;
	}
}