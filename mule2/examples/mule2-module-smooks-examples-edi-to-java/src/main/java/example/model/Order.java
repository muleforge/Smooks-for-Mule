/*
 *  Copyright 2008 Maurice Zeijen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package example.model;

import java.util.List;

/**
 *
 * @author maurice_zeijen
 *
 */
public class Order {
    private Header header;
    private List<OrderItem> orderItems;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
	public String toString() {
        StringBuffer desc = new StringBuffer();

        desc.append("Order Header: \n");
        desc.append(header);
        desc.append("Order Items: \n");
        for(int i = 0; i < orderItems.size(); i++) {
            desc.append("\t" + "(" + i +  "): " + orderItems.get(i)).append("\n");
        }

        return desc.toString();
    }
}
