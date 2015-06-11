/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package netty.mmb.http2.Server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP handler that responds with a "Hi,I'm mumubin!"
 */
public class Http1Handler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
//        关于100Continue的问题请看http://www.cnblogs.com/tekkaman/archive/2013/04/03/2997781.html
        if (HttpHeaderUtil.is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(req);


//        将反馈的内容写进去
        ByteBuf content = ctx.alloc().buffer();
        content.writeBytes(Http2Handler.RESPONSE_BYTES.duplicate());

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
//        设置头部
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
//            如果需要保持连接,设置下头部
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
