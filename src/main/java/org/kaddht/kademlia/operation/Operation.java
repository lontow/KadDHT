package org.kaddht.kademlia.operation;

import java.io.IOException;
import org.kaddht.kademlia.exceptions.RoutingException;

/**
 * Kademlia路由协议中的操作
 *
 * @author 刘朕龙
 * @created 20201016
 */
public interface Operation
{

    /**
     * 开始操作并在操作完成后返回
     *
     * @throws org.kaddht.kademlia.exceptions.RoutingException
     */
    public void execute() throws IOException, RoutingException;
}
