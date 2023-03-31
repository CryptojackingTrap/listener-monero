package it.unitn;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
//import rx.Subscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Online list of Monero Blocks:
 * https://localmonero.co/blocks
 */
public class MoneroListener {
    private static String mainNetUrl = "https://main-light.eth.linkpool.io";
    public static final Admin web3j = Admin.build(new HttpService(mainNetUrl));

    public static void main(String[] args) throws InterruptedException {
        EthBlock block = getLatestBlock();
        writeBlock(block);
        while (true) {
            EthBlock block2 = getLatestBlock();
            if ((!block.getBlock().getHash().equals(block2.getBlock().getHash()))) {
                block = block2;
                writeBlock(block);
            }
            Thread.sleep(1000);
        }
    }

    private static void writeBlock(EthBlock block) {
        showBlockInfo(block);
        String blockHash = block.getBlock().getHash();
        try {
            writeHash(blockHash, new Date(), new Date(block.getBlock().getTimestamp().longValue() * 1000));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static EthBlock getLatestBlock() {
        EthBlock latestBlock = null;
        try {
            latestBlock = web3j.ethGetBlockByNumber(
                    DefaultBlockParameterName.LATEST,
                    false).send();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return latestBlock;
    }

    public static void showBlockInfo(EthBlock block) {
        System.out.println("Block Hash: " + block.getBlock().getHash());
        System.out.println("Block Number: " + block.getBlock().getNumber());
        System.out.println("Block Size  : " + block.getBlock().getHash());
        System.out.println("Extra Data  : " + block.getBlock().getExtraData());
        System.out.println("Nonce       : " + block.getBlock().getNonce());
        System.out.println("Block Size  : " + block.getBlock().getSize());
    }

    //------------------------------- logging into file -----------------------
    private static final String DATE_FORMAT_PATTERN = "(yyyy/MM/dd, HH:mm:ss)";
    private final static String outFileRelativePath = "src/test/resources/latestBlockHash-Monero.txt";
    static FileChannel channel;

    static {
        File file = new File(outFileRelativePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(outFileRelativePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        channel = fout.getChannel();
    }

    private static void writeHash(String latestHash, Date receiveDate,
                                  Date blockDate) throws IOException {
        String genaralDateFormat = DATE_FORMAT_PATTERN;// hh for 12 format
        // clock and HH for
        // 24
        DateFormat dateFormat = new SimpleDateFormat(genaralDateFormat);
        String receiveDateStr = dateFormat.format(receiveDate);
        String blockDateStr = dateFormat.format(blockDate);
        // Integer lineSize = SHA_OUT_LENGTH_IN_HEX+
        // genaralDateFormat.length()*2 + 2;

        // buf.clear();
        String line = latestHash + " " + receiveDateStr + " " + blockDateStr
                + "\n";
        ByteBuffer buf = ByteBuffer.allocate(line.length());
        buf.put(line.getBytes());
        buf.flip();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }
    //----------------------------- end of logging into file ----------------
}

