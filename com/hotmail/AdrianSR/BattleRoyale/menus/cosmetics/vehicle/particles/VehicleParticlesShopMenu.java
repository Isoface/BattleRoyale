package com.hotmail.AdrianSR.BattleRoyale.menus.cosmetics.vehicle.particles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.hotmail.AdrianSR.BattleRoyale.config.lang.Lang;
import com.hotmail.AdrianSR.BattleRoyale.database.DTBPlayer;
import com.hotmail.AdrianSR.BattleRoyale.game.BRPlayer;
import com.hotmail.AdrianSR.BattleRoyale.game.managers.MoneyManager;
import com.hotmail.AdrianSR.BattleRoyale.main.BattleRoyale;
import com.hotmail.AdrianSR.BattleRoyale.menus.ItemClickEvent;
import com.hotmail.AdrianSR.BattleRoyale.menus.ItemMenu;
import com.hotmail.AdrianSR.BattleRoyale.menus.MenuItem;
import com.hotmail.AdrianSR.BattleRoyale.vehicles.BRVehicleParticle;
import com.hotmail.adriansr.core.util.console.ConsoleUtil;

import net.milkbowl.vault.economy.EconomyResponse;

//TODO: Terminar.
public final class VehicleParticlesShopMenu extends ItemMenu {
	
	private final DTBPlayer player;
	private       boolean can_open;

	public VehicleParticlesShopMenu(final DTBPlayer player) {
		// super implementation.
		super(Lang.VEHICLE_PARTICLES_SHOP_MENU_NAME.getValue(true), Size.SIX_LINE);
		
		// load player.
		this.player = player;
		
		// add items of selection.
		for (BRVehicleParticle particle : BRVehicleParticle.values()) {
			// check is not already purchased.
			if (!player.getPurchasedVehicleParticles().contains(particle)
					&& !particle.hasPermission(player.getMember().getPlayer())) {
				// register item.
				this.addItem(new VehicleParticlesShopItem(particle));

				// set can open this menu.
				this.can_open = true;
			}
		}
	}
	
	public void open() {
		// check member is online.
		if (player.getMember() != null && player.getMember().isOnline()) {
			// check can open this menu.
			if (this.can_open) {
				// open menu.
				this.open(player.getMember().getPlayer());
			} else {
				player.getMember().getPlayer().sendMessage(Lang.VEHICLE_PARTICLES_SHOP_CANNOT_OPEN.getValue(true));
			}
		}
	}

	@Override
	public void open(Player player) {
		// check DTB player.
		if (this.player != null && this.player.getMember() != null) {
			// open menu.
			super.open(this.player.getMember().getPlayer());
		}
	}

	private class VehicleParticlesShopItem extends MenuItem {
		
		private final BRVehicleParticle particle;

		public VehicleParticlesShopItem(BRVehicleParticle particle) {
			// super implementation.
			super(Lang.VEHICLE_PARTICLES_SHOP_ITEM_NAME.getValueReplacingWord(particle.getScreenName(), true), particle.getScreenIcon());
			
			// load particle.
			this.particle = particle;
		}
		
		@Override
		public void onItemClick(ItemClickEvent event) {
			// get and check battle royale player.
			final BRPlayer br_player = BRPlayer.getBRPlayer(event.getPlayer());
			if (br_player != null && br_player.getDatabasePlayer() != null) {
				// check money system.
				if (!MoneyManager.systemEnabled()) {
					// send not available money system message.
					event.getPlayer().sendMessage(Lang.SHOPS_PURCHASING_SYSTEM_NOT_AVAILABLE_MESSAGE.getValue(true));
					
					// print money system of message.
					ConsoleUtil.sendPluginMessage ( ChatColor.RED,
							"The money system is not enabled, your users cannot use cosmetics shops.",
							BattleRoyale.getInstance());
					return;
				}
				
				// check money.
				final int balance = (int) MoneyManager.getBalance(event.getPlayer());
				if (balance < particle.getCost()) {
					event.getPlayer().sendMessage(Lang.SHOPS_NOT_ENOUGH_MONEY_MESSAGE.getValue(true));
					return;
				}
				
				// PURCHASE:
				// discount money.
				final EconomyResponse transaction_reponse = MoneyManager.removeMoney(event.getPlayer(), particle.getCost());
				if (transaction_reponse == null || !transaction_reponse.transactionSuccess()) {
					return;
				}
				
				// set using vehicle.
				br_player.getDatabasePlayer().addPurchasedVehicleParticle(particle);
				
				// send vehicle selected message.
				event.getPlayer().sendMessage(Lang.VEHICLE_PARTICLES_PURCHASED_MESSAGE.getValue(true));
				
				// update database.
				br_player.getDatabasePlayer().save(false, false, true);
				
				// close menu.
				event.setWillClose(true);
			}
		}
	}
}
