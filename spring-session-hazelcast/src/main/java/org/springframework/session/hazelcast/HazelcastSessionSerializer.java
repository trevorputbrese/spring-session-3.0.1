/*
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.hazelcast;

import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import org.springframework.session.MapSession;

/**
 * A {@link com.hazelcast.nio.serialization.Serializer} implementation that handles the
 * (de)serialization of {@link MapSession} stored on {@link com.hazelcast.map.IMap}.
 *
 * <p>
 * The use of this serializer is optional and provides faster serialization of sessions.
 * If not configured to be used, Hazelcast will serialize sessions via
 * {@link java.io.Serializable} by default.
 *
 * <p>
 * If multiple instances of a Spring application is run, then all of them need to use the
 * same serialization method. If this serializer is registered on one instance and not
 * another one, then it will end up with HazelcastSerializationException. The same applies
 * when clients are configured to use this serializer but not the members, and vice versa.
 * Also note that, if a new instance is created with this serialization but the existing
 * Hazelcast cluster contains the values not serialized by this but instead the default
 * one, this will result in incompatibility again.
 *
 * <p>
 * An example of how to register the serializer on embedded instance can be seen below:
 *
 * <pre class="code">
 * Config config = new Config();
 *
 * // ... other configurations for Hazelcast ...
 *
 * SerializerConfig serializerConfig = new SerializerConfig();
 * serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
 * config.getSerializationConfig().addSerializerConfig(serializerConfig);
 *
 * HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
 * </pre>
 *
 * Below is the example of how to register the serializer on client instance. Note that,
 * to use the serializer in client/server mode, the serializer - and hence
 * {@link MapSession}, must exist on the server's classpath and must be registered via
 * {@link com.hazelcast.config.SerializerConfig} with the configuration above for each
 * server.
 *
 * <pre class="code">
 * ClientConfig clientConfig = new ClientConfig();
 *
 * // ... other configurations for Hazelcast Client ...
 *
 * SerializerConfig serializerConfig = new SerializerConfig();
 * serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
 * clientConfig.getSerializationConfig().addSerializerConfig(serializerConfig);
 *
 * HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
 * </pre>
 *
 * @author Enes Ozcan
 * @since 2.4.0
 */
public class HazelcastSessionSerializer implements StreamSerializer<MapSession> {

	private static final int SERIALIZER_TYPE_ID = 1453;

	@Override
	public void write(ObjectDataOutput out, MapSession session) throws IOException {
		out.writeString(session.getOriginalId());
		out.writeString(session.getId());
		writeInstant(out, session.getCreationTime());
		writeInstant(out, session.getLastAccessedTime());
		writeDuration(out, session.getMaxInactiveInterval());
		for (String attrName : session.getAttributeNames()) {
			Object attrValue = session.getAttribute(attrName);
			if (attrValue != null) {
				out.writeString(attrName);
				out.writeObject(attrValue);
			}
		}
	}

	private void writeInstant(ObjectDataOutput out, Instant instant) throws IOException {
		out.writeLong(instant.getEpochSecond());
		out.writeInt(instant.getNano());
	}

	private void writeDuration(ObjectDataOutput out, Duration duration) throws IOException {
		out.writeLong(duration.getSeconds());
		out.writeInt(duration.getNano());
	}

	@Override
	public MapSession read(ObjectDataInput in) throws IOException {
		String originalId = in.readString();
		MapSession cached = new MapSession(originalId);
		cached.setId(in.readString());
		cached.setCreationTime(readInstant(in));
		cached.setLastAccessedTime(readInstant(in));
		cached.setMaxInactiveInterval(readDuration(in));
		try {
			while (true) {
				// During write, it's not possible to write
				// number of non-null attributes without an extra
				// iteration. Hence the attributes are read until
				// EOF here.
				String attrName = in.readString();
				Object attrValue = in.readObject();
				cached.setAttribute(attrName, attrValue);
			}
		}
		catch (EOFException ignored) {
		}
		return cached;
	}

	private Instant readInstant(ObjectDataInput in) throws IOException {
		long seconds = in.readLong();
		int nanos = in.readInt();
		return Instant.ofEpochSecond(seconds, nanos);
	}

	private Duration readDuration(ObjectDataInput in) throws IOException {
		long seconds = in.readLong();
		int nanos = in.readInt();
		return Duration.ofSeconds(seconds, nanos);
	}

	@Override
	public int getTypeId() {
		return SERIALIZER_TYPE_ID;
	}

	@Override
	public void destroy() {
	}

}
