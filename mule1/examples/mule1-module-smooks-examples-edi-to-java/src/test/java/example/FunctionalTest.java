/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;
import org.mule.util.IOUtils;

import example.model.Customer;
import example.model.Header;
import example.model.Order;
import example.model.OrderItem;

/**
 * Unit test for this example
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class FunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {

		return "mule-edi-to-java-config.xml";
	}

	@Test
	public void testSmooks() throws Exception {
		InputStream in = IOUtils.getResourceAsStream("test-message01.edi", this.getClass());

		MuleClient client = new MuleClient();
		UMOMessage reply = client.send("vm://EDIOrderToJavaOrder",	new MuleMessage(in));

		assertNotNull(reply);
		assertNotNull(reply.getPayload());

		Object payload = reply.getPayload();

		assert payload instanceof Order : "The message payload is not an instance of Order";

		assertOrder((Order) payload);

		assert getReportFile().exists() : "The report file wasn't created";
	}

	private void assertOrder(Order order) {

		assertHeader(order.getHeader());
		assertOrderItems(order.getOrderItems());
	}


	private void assertHeader(Header header) {
		assert "1".equals(header.getOrderId());
		assert 0 == header.getOrderStatus();
		assert new BigDecimal("59.97").equals(header.getNetAmount());
		assert new BigDecimal("64.92").equals(header.getTotalAmount());
		assert new BigDecimal("4.95").equals(header.getTax());
		assert new GregorianCalendar(2006, 10, 15, 13, 45, 28).getTime().equals(header.getDate());

		assertCustomer(header.getCustomer());
	}

	private void assertCustomer(Customer customer) {

		assert "user1".equals(customer.getUserName());
		assert "Harry".equals(customer.getFirstName());
		assert "Fletcher".equals(customer.getLastName());
		assert "SD".equals(customer.getState());
	}

	/**
	 * @param orderItems
	 */
	private void assertOrderItems(List<OrderItem> orderItems) {

		assert 2 == orderItems.size();

		OrderItem orderItem = orderItems.get(0);

		assert 1 == orderItem.getQuantity();
		assert "364".equals(orderItem.getProductId());
		assert "The 40-Year-Old Virgin".equals(orderItem.getTitle());
		assert new BigDecimal("29.98").equals(orderItem.getPrice());

		orderItem = orderItems.get(1);

		assert 1 == orderItem.getQuantity();
		assert "299".equals(orderItem.getProductId());
		assert "Pulp Fiction".equals(orderItem.getTitle());
		assert new BigDecimal("29.99").equals(orderItem.getPrice());
	}


	private File getReportFile() {
		return new File("target/smooks-report/report.html");
	}

	private void deleteReportFile() {
		getReportFile().delete();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doPreFunctionalSetUp()
	 */
	@Override
	protected void doPreFunctionalSetUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		Locale.setDefault(new Locale("en","IE"));
		deleteReportFile();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.FunctionalTestCase#doFunctionalTearDown()
	 */
	@Override
	protected void doFunctionalTearDown() throws Exception {
		super.doFunctionalTearDown();

		deleteReportFile();
	}

}
