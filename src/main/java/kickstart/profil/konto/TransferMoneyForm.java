package kickstart.profil.konto;

/**
 * form to transfer Money
 */
public class TransferMoneyForm {
    private String amount, transferMethod;

    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransferMethod() {
        return transferMethod;
    }
    public void setTransferMethod(String transferMethod) {
        this.transferMethod = transferMethod;

    }
}
